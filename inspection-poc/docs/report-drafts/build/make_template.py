"""
시안D v5 → 시안D_v5_template.docx 변환 스크립트.

목적:
  점검내역서_시안D_v5.docx 의 정적 텍스트 일부를 ${field} placeholder 로 교체.
  10 SIGNATURE 섹션 (마지막 H10 paragraph + table#31) 을 삭제.

작동 원리:
  - word/document.xml 을 ET 로 파싱
  - paragraph 단위로 <w:t> 들의 텍스트를 합쳐서 알려진 텍스트와 매칭되면 치환
  - 매칭 시 첫 <w:t> 에 새 텍스트 두고 나머지 <w:t> 들의 text 를 비움 (구조/스타일 유지)
  - 표 셀의 결과/메모 컬럼은 (table_idx, row_idx, col_idx) 위치 기반으로 특정해서 ${...} 박음

사용법:
  python make_template.py <input.docx> <output.docx>
"""
import sys
import shutil
import zipfile
from xml.etree import ElementTree as ET
from copy import deepcopy

NS = {'w': 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'}
W = '{http://schemas.openxmlformats.org/wordprocessingml/2006/main}'
ET.register_namespace('w', NS['w'])

# ── 텍스트 치환 매핑 (paragraph-level) ──────────────────────────────
# 시안D 의 정확한 텍스트 → placeholder
PARAGRAPH_REPLACEMENTS = {
    # 표지
    '단양군청  |  (주)정도유아이티':            '${cover.headerLine}',  # 표지 상단 라인
    '월간 운영·유지관리 점검 보고':              '${cover.docSubtitle}',
    '2026년 단양군 도시계획정보체계(UPIS) 운영 및 유지관리': '${cover.projectFullName}',
    '본 보고서는 단양군청 도시계획정보체계(UPIS) 운영장비의 2026년 5월 정기점검 결과를 정리합니다. AP 서버 · DB 서버 · GIS 엔진 3 tier 및 표준시스템(UPIS 애플리케이션) 의 자동수집 메트릭과 현장 육안 점검을 통합하여 운영 상태와 차회 점검 조치 권고사항을 종합하였습니다.': '${cover.notice}',
    # SUMMARY 본문
    '본 회차는 단양군청 도시계획정보체계(UPIS) 운영장비의 5월 정기점검 (제 5 회차) 입니다. AP 서버 (Win Server 2012 R2) · DB 서버 (AIX 6.1 + Oracle 11g R2) · GIS 엔진 (GeoNURIS GSS/GWS) 의 자동수집 결과는 정상 36 건 / 경고 2 건 / 에러 0 건 입니다. 추가 점검(육안)은 8 건이며 모두 정상 응답하였습니다.': '${summary.body}',
    '핵심 발견사항은 (1) GWS geowebservice64-stdout 로그 로테이션 미설정, (2) UWES store 용량의 점진 증가, (3) Oracle UPIS_DATA 테이블스페이스 사용률 증가 추이입니다. 권고사항 §5 에 후속 조치를 기재합니다.': '${summary.findings}',
    # HISTORY 본문
    '· 2026년 연간 정기점검 및 장애조치 이력 — 1~5월 점검 완료, 6월 이후 예정.': '${history.note}',
    # TARGETS 본문
    '· DB 서버 / AP 서버 / 소프트웨어 — 본 회차 점검대상 H/W·S/W 사양.': '${targets.note}',
    # 04 AP 본문
    '· IBM X3650 M4 / Win Server 2012 R2 / UPIS AP 서버.': '${ap.note}',
    '※ 추가 점검 — CPU 23.4 % · Memory 41.2 % · Disk C 60.3 % / D 22.1 %': '${ap.extraNote}',
    # 05 DB 본문
    '· IBM P720 / AIX 6.1 / UPIS DB 서버 — 육안·구성·성능·DATA·네트워크·프로세서·로그.': '${db.note}',
    '※ 추가 점검 — CPU 18.6 % · Memory 38.4 % | 파일시스템: / 32 % · /backup 41 % · /oracle 28 % · /oradata 64 % · /Archive 19 %': '${db.extraNote}',
    # 06 DBMS 본문
    '· Oracle Standard Edition Two / 11g R2 — 환경·로그·아카이브·구성·용량·백업.': '${dbms.note}',
    '※ 추가 점검 — UPIS_DATA 테이블스페이스 78.4 % (임계 80 % 근접) — 차회 점검 전 모니터링 권고': '${dbms.extraNote}',
    # 07 GIS 본문
    '· GeoNURIS GSS (Spatial Server) / GWS (GeoWeb Server 64) / Desktop Pro 연계.': '${gis.note}',
    '※ 추가 점검 — DEM·SLOP 파일은 영구 보존 대상 (삭제 제외). GWS stdout 로테이션 미설정 — 권고사항 §5 참조': '${gis.extraNote}',
    '· 보조 — UWES Store 자동수집 메트릭 (30 일 요약).': '${gis.uwesNote}',
    # 08 APPLICATION 본문
    '· 도시계획 / 통계 / 전자심의 / 지구단위계획 / 비정형 / 관리자 / GIS 연동 — 14 개 메뉴.': '${app.note}',
    # 09 NEXT ROUND 본문
    '담당자와 협의 후 6월에 점검자 박욱진 수행 예정': '${next.scheduleNote}',
    '1) GWS geowebservice64-stdout 로그 로테이션 미설정 → logback / log4j2 size-based rotation 적용 권고.': '${next.recommendation1}',
    '2) UWES store 용량 18.4 GB 로 임계치 20 GB 근접 — DEM·SLOP 외 임시 파일 정리 주기 단축 검토.':         '${next.recommendation2}',
    '3) Oracle 테이블스페이스 UPIS_DATA 78.4 % — 차회 점검 전 데이터 증가 추이 모니터링 + 80 % 도달 시 확장 계획 수립.': '${next.recommendation3}',
    '1) GWS 로그 로테이션 적용 — 수행사 6월 중순 적용 예정.':                                  '${next.followup1}',
    '2) Store 임시파일 정리 — 차회 점검 시 일괄 처리.':                                        '${next.followup2}',
    '3) Oracle 테이블스페이스 모니터링 — 일간 자동 알림 설정 (DBA 측 협의).':                  '${next.followup3}',
}

# ── 표지 (table#3, #4) 의 셀 텍스트 치환 ──
# (table_idx, row_idx, col_idx, paragraph_text → new_text)
CELL_TEXT_REPLACEMENTS = [
    # Table #3 (회차/작성일/점검범위)
    (3, 0, 1, '2026년 5월 정기점검',       '${cover.roundLabel}'),
    (3, 1, 1, '2026년 5월 13일',           '${cover.reportDate}'),
    (3, 2, 1, 'AP · DB · GIS · 표준시스템', '${cover.scope}'),
    # Table #4 (확인자/점검자)
    (4, 1, 1, '단양군청',                  '${cover.clientOrg}'),
    (4, 1, 3, '(주)정도유아이티',          '${cover.inspectorOrg}'),
    (4, 2, 1, '(공무원 담당자)',           '${cover.clientName}'),
    (4, 2, 3, '박욱진',                    '${cover.inspectorName}'),
    # Table #6 SUMMARY — 각 셀의 숫자 paragraph 만 동적
    (6, 0, 0, '46 건', '${summary.totalCnt}'),
    (6, 0, 1, '36 건', '${summary.okCnt}'),
    (6, 0, 2, '2 건',  '${summary.warnCritCnt}'),
    (6, 0, 3, '8 건',  '${summary.manualCnt}'),
    # Table #10 (DB 서버 사양)
    (10, 0, 1, 'IBM P720',                              '${targets.db.model}'),
    (10, 1, 1, 'POWER7 3.6GHz 4 Core',                  '${targets.db.cpu}'),
    (10, 2, 1, '32 GB',                                 '${targets.db.memory}'),
    (10, 3, 1, '300 GB SAS 10Krpm × 4 (미러링 구성)',   '${targets.db.disk}'),
    (10, 4, 1, 'Onboard 10/100/1000-TX × 4 Port',       '${targets.db.network}'),
    (10, 5, 1, '이중화 전원',                            '${targets.db.power}'),
    (10, 6, 1, 'IBM AIX 6.1',                           '${targets.db.os}'),
    # Table #11 (AP 서버 사양)
    (11, 0, 1, 'IBM X3650 M4',                                    '${targets.ap.model}'),
    (11, 1, 1, 'Intel Xeon 3.6GHz × 4 Core',                      '${targets.ap.cpu}'),
    (11, 2, 1, '16 GB',                                           '${targets.ap.memory}'),
    (11, 3, 1, '300 GB SAS 10Krpm × 6',                           '${targets.ap.disk}'),
    (11, 4, 1, '10/100/1000-TX × 4 Port',                         '${targets.ap.network}'),
    (11, 5, 1, '이중화 전원',                                      '${targets.ap.power}'),
    (11, 6, 1, 'WinSvrStd 2012 SNGL OLP NL 2Proc / WinSvrExtConn 2012', '${targets.ap.os}'),
    # Table #14 (AP 헤더)
    (14, 0, 1, 'Win Server 2012 R2',           '${ap.head.os}'),
    (14, 0, 3, 'Intel Xeon 3.6GHz (4 Core)',   '${ap.head.cpu}'),
    (14, 1, 1, 'UPIS AP 서버',                  '${ap.head.usage}'),
    (14, 1, 3, '16 GB',                         '${ap.head.memory}'),
    (14, 2, 1, 'IBM X3650 M4',                  '${ap.head.model}'),
    (14, 2, 3, '300 GB × 6 EA',                 '${ap.head.disk}'),
    # Table #17 (DB 헤더)
    (17, 0, 1, 'IBM P720',         '${db.head.model}'),
    (17, 0, 3, 'AIX 6.1',           '${db.head.os}'),
    (17, 1, 1, 'UPIS-DB',           '${db.head.hostname}'),
    (17, 1, 3, 'UPIS DB 서버',      '${db.head.usage}'),
    # Table #20 (DBMS 헤더)
    (20, 0, 1, 'UPIS DB',           '${dbms.head.target}'),
    (20, 0, 3, 'Oracle DB',         '${dbms.head.software}'),
    (20, 1, 1, 'AIX 6.1',           '${dbms.head.os}'),
    (20, 1, 3, '107.11.103.82',     '${dbms.head.ip}'),
]

# ── 점검표 결과/메모 column 자동 placeholder 박기 ──
# 표 인덱스, 헤더 row 제외 r=1..N, 결과/메모 cell idx, ID prefix
RESULT_TABLES = [
    # (table_idx, header_row_count, result_col, memo_col, id_prefix, expected_row_count)
    (15, 1, 4, 5, 'ap',   14),  # AP 점검표 (R1-R14)
    (18, 1, 3, 4, 'db',   24),  # DB 점검표 (R1-R24)
    (21, 1, 3, 4, 'dbms', 17),  # DBMS 점검표 (R1-R17)
    (23, 1, 3, 4, 'gis',   6),  # GIS 점검표 (R1-R6)
    (28, 1, 3, 4, 'app',  14),  # APP 점검표 (R1-R14)
]

# ── HISTORY 표 (#8) 동적 행 ──
HISTORY_TABLE_IDX = 8
HISTORY_HEADER_ROWS = 1   # R0 헤더
HISTORY_DATA_ROWS  = 12   # R1-R12 = 1~12월
# col idx: 0=년, 1=월, 2=일, 3=업무, 4=증상, 5=조치
HISTORY_COLS = ['year', 'month', 'day', 'work', 'symptom', 'action']

# ── 삭제 대상: 10 SIGNATURE 섹션 ──
# H10 paragraph 부터 table #31 까지 (확인 후 결정)
SECTIONS_TO_REMOVE = ['10 · SIGNATURE', '확인 · 서명']


def text_of_paragraph(p):
    return ''.join((t.text or '') for t in p.findall(f'.//{W}t'))


def set_paragraph_text(p, new_text):
    """paragraph 의 모든 <w:t> 중 첫 번째에 new_text 두고 나머지는 비움.
    (스타일/구조 유지하면서 텍스트만 단일 run 으로 통합)"""
    ts = list(p.findall(f'.//{W}t'))
    if not ts:
        return False
    ts[0].text = new_text
    # xml:space='preserve' 보장
    ts[0].set('{http://www.w3.org/XML/1998/namespace}space', 'preserve')
    for t in ts[1:]:
        t.text = ''
    return True


def replace_paragraphs_in_element(elem, mapping, stats):
    for p in elem.findall(f'.//{W}p'):
        full_text = text_of_paragraph(p).strip()
        if full_text in mapping:
            set_paragraph_text(p, mapping[full_text])
            stats['paragraph'] += 1


def find_tables(body):
    """body 의 최상위 + 1단계 nested tables 까지 모두 가져옴."""
    return list(body.iter(f'{W}tbl'))


def replace_cell_text(tables, table_idx, row_idx, col_idx, expected_text, new_text, stats):
    if table_idx >= len(tables):
        return False
    tbl = tables[table_idx]
    rows = list(tbl.findall(f'.//{W}tr'))
    if row_idx >= len(rows):
        return False
    cells = list(rows[row_idx].findall(f'./{W}tc'))
    if col_idx >= len(cells):
        return False
    cell = cells[col_idx]
    paragraphs = cell.findall(f'.//{W}p')
    for p in paragraphs:
        if text_of_paragraph(p).strip() == expected_text:
            set_paragraph_text(p, new_text)
            stats['cell'] += 1
            return True
    return False


def inject_result_memo_placeholders(tables, stats):
    """결과/메모 컬럼 자동 placeholder 박기."""
    for (t_idx, hdr_rows, res_col, memo_col, prefix, _exp_n) in RESULT_TABLES:
        if t_idx >= len(tables):
            continue
        tbl = tables[t_idx]
        rows = list(tbl.findall(f'.//{W}tr'))
        for r_idx in range(hdr_rows, len(rows)):
            data_idx = r_idx - hdr_rows + 1    # r1, r2, ...
            cells = list(rows[r_idx].findall(f'./{W}tc'))
            # 결과
            if res_col < len(cells):
                ps = cells[res_col].findall(f'.//{W}p')
                if ps:
                    set_paragraph_text(ps[0], f'${{{prefix}.r{data_idx}.result}}')
                    for p in ps[1:]:
                        set_paragraph_text(p, '')
                    stats['result'] += 1
            # 메모
            if memo_col < len(cells):
                ps = cells[memo_col].findall(f'.//{W}p')
                if ps:
                    set_paragraph_text(ps[0], f'${{{prefix}.r{data_idx}.memo}}')
                    for p in ps[1:]:
                        set_paragraph_text(p, '')
                    stats['memo'] += 1


def inject_history_placeholders(tables, stats):
    """HISTORY (#8) 12개월 행의 각 cell 에 ${history.M01.field} 박기."""
    if HISTORY_TABLE_IDX >= len(tables):
        return
    tbl = tables[HISTORY_TABLE_IDX]
    rows = list(tbl.findall(f'.//{W}tr'))
    for m in range(1, HISTORY_DATA_ROWS + 1):
        r_idx = HISTORY_HEADER_ROWS + m - 1
        if r_idx >= len(rows):
            break
        cells = list(rows[r_idx].findall(f'./{W}tc'))
        for c_idx, field_name in enumerate(HISTORY_COLS):
            if c_idx >= len(cells):
                continue
            ps = cells[c_idx].findall(f'.//{W}p')
            if ps:
                set_paragraph_text(ps[0], f'${{history.M{m:02d}.{field_name}}}')
                for p in ps[1:]:
                    set_paragraph_text(p, '')
                stats['history'] += 1


def remove_signature_section(body):
    """body 의 직계 child 중 SIGNATURE 헤더 paragraph 부터 끝까지(또는 마지막 sectPr 직전까지) 모두 제거."""
    children = list(body)
    remove_started = False
    to_remove = []
    for child in children:
        tag = child.tag.split('}')[-1]
        if not remove_started and tag == 'p':
            txt = text_of_paragraph(child).strip()
            if '10 · SIGNATURE' in txt or txt == '10 · SIGNATURE':
                remove_started = True
        if remove_started:
            # 마지막 sectPr 는 보존 (페이지 설정)
            if tag == 'sectPr':
                continue
            to_remove.append(child)
    for child in to_remove:
        body.remove(child)
    return len(to_remove)


def main(src_path, dst_path):
    shutil.copy(src_path, dst_path)

    # zip 내용 읽고 document.xml 만 변환
    with zipfile.ZipFile(dst_path, 'r') as z_in:
        names = z_in.namelist()
        contents = {name: z_in.read(name) for name in names}

    doc_xml = contents['word/document.xml']
    tree = ET.ElementTree(ET.fromstring(doc_xml))
    root = tree.getroot()
    body = root[0]
    tables = find_tables(body)

    stats = {'paragraph': 0, 'cell': 0, 'result': 0, 'memo': 0, 'history': 0}

    # 1) paragraph-level 텍스트 치환
    replace_paragraphs_in_element(body, PARAGRAPH_REPLACEMENTS, stats)

    # 2) cell 위치 기반 텍스트 치환
    for (t, r, c, exp, new) in CELL_TEXT_REPLACEMENTS:
        replace_cell_text(tables, t, r, c, exp, new, stats)

    # 3) HISTORY 동적 행
    inject_history_placeholders(tables, stats)

    # 4) 점검표 결과/메모
    inject_result_memo_placeholders(tables, stats)

    # 5) SIGNATURE 섹션 제거
    removed = remove_signature_section(body)

    # write back
    out_xml = ET.tostring(root, encoding='UTF-8', xml_declaration=True)
    contents['word/document.xml'] = out_xml

    with zipfile.ZipFile(dst_path, 'w', zipfile.ZIP_DEFLATED) as z_out:
        for name in names:
            z_out.writestr(name, contents[name])

    print(f'OK template written: {dst_path}')
    print(f'  paragraph replacements: {stats["paragraph"]}')
    print(f'  cell text replacements: {stats["cell"]}')
    print(f'  history placeholders  : {stats["history"]}')
    print(f'  result placeholders   : {stats["result"]}')
    print(f'  memo placeholders     : {stats["memo"]}')
    print(f'  signature children removed: {removed}')


if __name__ == '__main__':
    src = sys.argv[1] if len(sys.argv) > 1 else 'C:/Users/ukjin/sw-management-system/inspection-poc/docs/report-drafts/점검내역서_시안D_v5.docx'
    dst = sys.argv[2] if len(sys.argv) > 2 else 'C:/Users/ukjin/sw-management-system/src/main/resources/templates/inspection-report/시안D_v5_template.docx'
    import os
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    main(src, dst)
