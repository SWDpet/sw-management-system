# -*- coding: utf-8 -*-
"""
commence_body.hwpx 템플릿의 하드코딩된 값을 {{플레이스홀더}}로 변환하는 스크립트
- 현장대리인 정보 (주소, 성명, 주민번호, 생년월일, 자격)
- 보안각서 인력목록 (3명 → 플레이스홀더)
- 과업참여자 명단 (3명 → 플레이스홀더)
- 보안서약서 (4부 → 플레이스홀더)
- 착수계 날짜
- 과업수행계획서 투입인력
"""
import os, sys, zipfile, shutil, re
from io import BytesIO

TEMPLATE_PATH = os.path.join(os.path.dirname(__file__), 'commence_body.hwpx')
OUTPUT_PATH = os.path.join(os.path.dirname(__file__), 'commence_body.hwpx')
BACKUP_PATH = os.path.join(os.path.dirname(__file__), 'commence_body_backup.hwpx')

def replace_text_in_xml(xml, old_text, new_text):
    """XML 내 텍스트 노드에서 old_text를 new_text로 치환"""
    # HWPX XML에서 텍스트는 <hp:t>텍스트</hp:t> 패턴
    count = 0
    # 정확히 텍스트 노드 내용만 교체
    pattern = re.compile(r'(>)(' + re.escape(old_text) + r')(</)')
    result = pattern.sub(lambda m: m.group(1) + new_text + m.group(3), xml)
    count = len(pattern.findall(xml))
    if count == 0:
        # 직접 문자열 치환 시도
        if old_text in xml:
            result = xml.replace(old_text, new_text)
            count = xml.count(old_text)
    return result, count

def process_template():
    # 백업
    if not os.path.exists(BACKUP_PATH):
        shutil.copy2(TEMPLATE_PATH, BACKUP_PATH)
        print(f"백업 생성: {BACKUP_PATH}")

    # ZIP 읽기
    with open(TEMPLATE_PATH, 'rb') as f:
        template_bytes = f.read()

    entries = {}
    with zipfile.ZipFile(BytesIO(template_bytes), 'r') as zin:
        for info in zin.infolist():
            entries[info.filename] = (info, zin.read(info.filename))

    # section0.xml 처리
    xml_data = entries['Contents/section0.xml'][1].decode('utf-8')

    replacements = []

    # ── 1. 착수계 날짜 (계약년월일, 착수년월일, 준공예정일) ──
    # 계약년월일: 2025년 12월 30일 → 플레이스홀더
    # 이건 XML 내 위치가 고정적이므로 순서대로 치환
    # 착수계 본문의 "2025년" (첫번째) → {{계약년도}}년
    # 하지만 여러 곳에 2025년, 2026년이 있으므로 주의 필요

    # ── 2. 현장대리인계 ──
    xml_data, c = replace_text_in_xml(xml_data,
        '서울특별시 중랑구 신내로 110 동성 3차 아파트 14동 2206호', '{{현대_주소}}')
    replacements.append(('현장대리인 주소', c))

    # 김한준은 여러 곳에 있음. 현장대리인(1), 보안각서(1), 보안서약서(1), 투입인력(1)
    # 순서대로 치환해야 함
    # 현장대리인의 김한준을 {{현대_성명}}으로
    # 보안각서의 김한준을 {{보안2_성명}}으로
    # 보안서약서의 김한준을 {{서약2_성명}}으로
    # 투입인력의 김한준을 {{인력2_성명}}으로

    # 순차 치환을 위해 위치 기반으로 처리
    # 먼저 모든 김한준 위치 파악
    positions = [m.start() for m in re.finditer('김한준', xml_data)]
    print(f"김한준 위치: {positions}")

    # 박욱진 위치
    positions_park = [m.start() for m in re.finditer('박욱진', xml_data)]
    print(f"박욱진 위치: {positions_park}")

    # 서현규 위치
    positions_seo = [m.start() for m in re.finditer('서현규', xml_data)]
    print(f"서현규 위치: {positions_seo}")

    # 830211-1****** 위치
    positions_ssn = [m.start() for m in re.finditer('830211-1\\*{6}', xml_data)]
    print(f"830211 위치: {positions_ssn}")

    # --- 순차 치환: 뒤에서부터 (위치가 변하지 않도록) ---

    # 인력 정보 매핑 (문서 순서):
    # 현장대리인계: 김한준, 830211-1******, 1983년, 2월, 11일, -
    # 보안각서: 박욱진/고급/830211.../GIS, 김한준/고급/830211.../GIS, 서현규/중급/830211.../GIS (SSN은 다를수있지만 템플릿에서 동일값)
    # 과업참여자: 유지보수책임기술자/이사/학력기술자, 참여기술자/차장/학력기술자, 참여기술자/과장/학력기술자
    # 보안서약서: 박욱진/이사(책임), 김한준/차장(참여), 서현규/과장(참여), 노성기/대표이사(대표자)
    # 투입인력: 박욱진/이사/고급, 김한준/차장/고급, 서현규/과장/중급

    # 전략: 구체적인 문맥 패턴으로 치환
    # 현장대리인계 영역의 김한준
    xml_data = xml_data.replace('830211-1******', '{{SSN_PLACEHOLDER}}', 1)  # 현대 SSN (첫번째)

    # 1983년 → {{현대_생년}}년
    xml_data = xml_data.replace('>1983년<', '>{{현대_생년}}년<', 1)

    # 현장대리인의 김한준 (첫번째 김한준)
    xml_data = xml_data.replace('>김한준<', '>{{현대_성명}}<', 1)

    # 보안각서 영역: 박욱진, 김한준, 서현규 + 등급 + 업무
    # 박욱진/고급/GIS S/W 유지보수 (첫번째)
    xml_data = xml_data.replace('>박욱진<', '>{{보안1_성명}}<', 1)
    xml_data = xml_data.replace('>고급<', '>{{보안1_등급}}<', 1)
    xml_data = xml_data.replace('>GIS S/W 유지보수<', '>{{보안1_업무}}<', 1)
    xml_data = xml_data.replace('{{SSN_PLACEHOLDER}}', '{{보안1_주민번호}}', 1)

    # 김한준/고급/GIS S/W 유지보수 (두번째 김한준)
    xml_data = xml_data.replace('>김한준<', '>{{보안2_성명}}<', 1)
    xml_data = xml_data.replace('>고급<', '>{{보안2_등급}}<', 1)
    xml_data = xml_data.replace('>GIS S/W 유지보수<', '>{{보안2_업무}}<', 1)
    xml_data = xml_data.replace('830211-1******', '{{보안2_주민번호}}', 1)

    # 서현규/중급/GIS S/W 유지보수 (첫번째 서현규)
    xml_data = xml_data.replace('>서현규<', '>{{보안3_성명}}<', 1)
    xml_data = xml_data.replace('>중급<', '>{{보안3_등급}}<', 1)
    xml_data = xml_data.replace('>GIS S/W 유지보수<', '>{{보안3_업무}}<', 1)
    xml_data = xml_data.replace('830211-1******', '{{보안3_주민번호}}', 1)

    # 과업참여자 명단: 직위/자격
    xml_data = xml_data.replace('>이사<', '>{{참여1_직위}}<', 1)
    xml_data = xml_data.replace('>학력기술자<', '>{{참여1_자격}}<', 1)
    xml_data = xml_data.replace('>차장<', '>{{참여2_직위}}<', 1)
    xml_data = xml_data.replace('>학력기술자<', '>{{참여2_자격}}<', 1)
    xml_data = xml_data.replace('>과장<', '>{{참여3_직위}}<', 1)
    xml_data = xml_data.replace('>학력기술자<', '>{{참여3_자격}}<', 1)

    # 보안서약서 1 (책임기술자 - 박욱진)
    xml_data = xml_data.replace('>이사<', '>{{서약1_직위}}<', 1)
    xml_data = xml_data.replace('>박욱진<', '>{{서약1_성명}}<', 1)

    # 보안서약서 2 (참여기술자 - 김한준)
    xml_data = xml_data.replace('>차장<', '>{{서약2_직위}}<', 1)
    xml_data = xml_data.replace('>김한준<', '>{{서약2_성명}}<', 1)

    # 보안서약서 3 (참여기술자 - 서현규)
    xml_data = xml_data.replace('>과장<', '>{{서약3_직위}}<', 1)
    xml_data = xml_data.replace('>서현규<', '>{{서약3_성명}}<', 1)

    # 보안서약서 4 (대표자 - 노성기/대표이사) - 고정값이므로 치환 안함

    # 투입인력 정보: 박욱진/이사/고급, 김한준/차장/고급, 서현규/과장/중급
    xml_data = xml_data.replace('>박욱진<', '>{{인력1_성명}}<', 1)
    xml_data = xml_data.replace('>이사<', '>{{인력1_직급}}<', 1)
    xml_data = xml_data.replace('>고급<', '>{{인력1_등급}}<', 1)

    xml_data = xml_data.replace('>김한준<', '>{{인력2_성명}}<', 1)
    xml_data = xml_data.replace('>차장<', '>{{인력2_직급}}<', 1)
    xml_data = xml_data.replace('>고급<', '>{{인력2_등급}}<', 1)

    xml_data = xml_data.replace('>서현규<', '>{{인력3_성명}}<', 1)
    xml_data = xml_data.replace('>과장<', '>{{인력3_직급}}<', 1)
    xml_data = xml_data.replace('>중급<', '>{{인력3_등급}}<', 1)

    # 현장대리인 SSN 플레이스홀더 (이미 보안 쪽에서 처리됨, 현대용 추가)
    # {{현대_생월}}, {{현대_생일}}
    # "2월" → {{현대_생월}}월, "11일" → {{현대_생일}}일
    # 이건 XML에서 정확한 위치를 찾아야 함
    # 현장대리인 영역 근처의 "2월", "11일"

    # 자격: "-" → {{현대_자격}}
    # 현장대리인 영역의 "-"는 너무 일반적이므로 skip

    # 착수일자: "2026년 01월 01일부로" → "{{착수일자_한글}}부로" (보안서약서에서 4회)
    xml_data = xml_data.replace('2026년 01월 01일부로', '{{착수일자_한글}}부로')

    # 현대 SSN
    xml_data = xml_data.replace('>{{SSN_PLACEHOLDER}}<', '>{{현대_주민번호}}<')
    # 혹시 남아있으면
    xml_data = xml_data.replace('{{SSN_PLACEHOLDER}}', '{{현대_주민번호}}')

    # 검증: 남은 하드코딩 확인
    print("\n=== 검증: 남은 하드코딩 확인 ===")
    remaining_checks = ['김한준', '박욱진', '서현규', '830211']
    for kw in remaining_checks:
        cnt = xml_data.count(kw)
        print(f"  {kw}: {cnt}회 남음")

    # 새로 추가된 플레이스홀더 확인
    new_placeholders = re.findall(r'{{[^}]+}}', xml_data)
    unique_ph = sorted(set(new_placeholders))
    print(f"\n=== 전체 플레이스홀더 ({len(unique_ph)}개) ===")
    for ph in unique_ph:
        print(f"  {ph}: {new_placeholders.count(ph)}회")

    # Preview/PrvText.txt 처리
    prv_data = entries['Preview/PrvText.txt'][1].decode('utf-8')
    prv_data = prv_data.replace('서울특별시 중랑구 신내로 110 동성 3차 아파트 14동 2206호', '{{현대_주소}}')
    prv_data = prv_data.replace('김한준', '{{현대_성명}}', 1)
    prv_data = prv_data.replace('830211-1******', '{{현대_주민번호}}', 1)
    prv_data = prv_data.replace('1983년', '{{현대_생년}}년', 1)

    # ZIP 재작성
    output = BytesIO()
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as zout:
        for name, (info, data) in entries.items():
            if name == 'Contents/section0.xml':
                data = xml_data.encode('utf-8')
            elif name == 'Preview/PrvText.txt':
                data = prv_data.encode('utf-8')
            zout.writestr(info, data)

    with open(OUTPUT_PATH, 'wb') as f:
        f.write(output.getvalue())

    print(f"\n완료! 템플릿 갱신: {OUTPUT_PATH}")

if __name__ == '__main__':
    process_template()
