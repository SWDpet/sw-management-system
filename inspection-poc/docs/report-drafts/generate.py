"""
점검내역서 시안 3종 (.docx) 생성기 — 시안 A 클래식 / B 모던 / C 미니멀.

요건:
  • 표지: 사업명 / 월 / 기관(지자체·고객) / 확인자(담당자=공무원·고객) 소속·성명·서명란 / 점검자 동일
  • 2페이지: 월별 점검사항 표 (커스터마이징 가능)
  • 나머지: 발견사항·조치, 종합의견, 차회 점검, 첨부

예시값은 단양군 UPIS 사례. 양식 자체에 영향 없도록 모든 텍스트는 [대괄호] 또는 빈칸으로.
"""
from __future__ import annotations

from copy import deepcopy
from pathlib import Path

from docx import Document
from docx.enum.table import WD_ALIGN_VERTICAL, WD_ROW_HEIGHT_RULE
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml.ns import qn, nsmap
from docx.oxml import OxmlElement
from docx.shared import Cm, Pt, RGBColor

OUT_DIR = Path(__file__).parent


# ── 공통 데이터 ─────────────────────────────────────────────────────────────
# 예시값은 단양군 UPIS 사례 — 시안 비교용 자리표시자.
COVER_DEFAULTS = {
    "사업명": "20○○년 ○○○ 시스템 운영 및 유지관리",
    "회차": "20○○년 ○월 정기점검 (제 회차)",
    "기관": "○○군청 / ○○시청 / ○○공사 ○○○실",
    "기관담당부서": "정보화담당관 / 정보화과 등",
    "수행사": "(주)○○○",
    "작성일자": "20○○년 ○월 ○○일",
}

# 월별 커스터마이징 가능 — 행 추가/삭제·항목 변경 자유
INSPECTION_ITEMS = [
    # (분류, 점검항목, 비고)
    ("AP 서버", "CPU·메모리 정보 확인", "ap.hw.*"),
    ("AP 서버", "어댑터 / 디스크 구성 확인", "ap.hw.adapter, ap.disk.*"),
    ("AP 서버", "CPU 사용률", "ap.perf.cpu_pct"),
    ("AP 서버", "메모리 사용률", "ap.perf.mem_pct"),
    ("AP 서버", "시스템 이벤트 로그 (에러)", "ap.log.system_err"),
    ("AP 서버", "보안 이벤트 로그 (에러)", "ap.log.security_err"),
    ("AP 서버", "네트워크 라우팅 / IP", "ap.net.*"),
    ("AP 서버", "사용자 계정 점검", "ap.security.users"),
    ("AP 서버", "LED — 시스템 / PSU / 디스크 / AP 케이블", "ap.led.* (육안 M)"),
    ("GIS", "GSS 프로세스 가동 상태", "gis.gss.running"),
    ("GIS", "GSS 로그 정리 (보존정책)", "gis.gss.log_purge"),
    ("GIS", "GWS 프로세스 가동 상태", "gis.gws.running"),
    ("GIS", "GWS 로그 정리", "gis.gws.log_purge"),
    ("GIS", "GWS store 용량", "gis.gws.store_size"),
    ("GIS", "GWS HTTP 응답", "gis.gws.http"),
    ("DB", "인스턴스 가동 / 알림 로그", "db.alert"),
    ("DB", "테이블스페이스 사용률", "db.tbs"),
    ("DB", "백업 결과 / 보존", "db.backup"),
    ("DB", "Long-running 세션 / 락", "db.session"),
    ("기타", "백업 매체 보존 / 외부 반출 이력", ""),
    ("기타", "보안 패치 / 펌웨어 업데이트 검토", ""),
    ("기타", "기타 특이사항", ""),
]

FINDINGS_ROWS = 5      # 발견사항/조치 빈 행
ATTACH_ROWS = 4        # 첨부 빈 행


# ── 공용 헬퍼 ───────────────────────────────────────────────────────────────
def set_page(doc: Document, margin_cm=2.0):
    for s in doc.sections:
        s.page_width = Cm(21.0)
        s.page_height = Cm(29.7)
        s.top_margin = Cm(margin_cm)
        s.bottom_margin = Cm(margin_cm)
        s.left_margin = Cm(margin_cm)
        s.right_margin = Cm(margin_cm)


def set_run(run, *, font="맑은 고딕", size=10, bold=False, color=None):
    run.font.name = font
    run.font.size = Pt(size)
    run.bold = bold
    if color is not None:
        run.font.color.rgb = RGBColor(*color)
    # east-asian font binding
    r = run._element
    rPr = r.get_or_add_rPr()
    rFonts = rPr.find(qn("w:rFonts"))
    if rFonts is None:
        rFonts = OxmlElement("w:rFonts")
        rPr.append(rFonts)
    rFonts.set(qn("w:eastAsia"), font)
    rFonts.set(qn("w:ascii"), font)
    rFonts.set(qn("w:hAnsi"), font)


def add_para(doc_or_cell, text="", *, align=None, **run_kw):
    p = doc_or_cell.add_paragraph()
    if align == "center":
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    elif align == "right":
        p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    r = p.add_run(text)
    set_run(r, **run_kw)
    return p


def shade_cell(cell, hex_color: str):
    tcPr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    tcPr.append(shd)


def set_cell_borders(cell, *, sides=("top", "bottom", "left", "right"),
                     sz=8, color="000000", val="single"):
    """sz is in 1/8 pt — 8 means 1pt, 16 means 2pt."""
    tcPr = cell._tc.get_or_add_tcPr()
    tcBorders = tcPr.find(qn("w:tcBorders"))
    if tcBorders is None:
        tcBorders = OxmlElement("w:tcBorders")
        tcPr.append(tcBorders)
    for side in sides:
        b = tcBorders.find(qn(f"w:{side}"))
        if b is None:
            b = OxmlElement(f"w:{side}")
            tcBorders.append(b)
        b.set(qn("w:val"), val)
        b.set(qn("w:sz"), str(sz))
        b.set(qn("w:color"), color)


def set_row_height(row, cm: float, rule="atLeast"):
    trPr = row._tr.get_or_add_trPr()
    h = OxmlElement("w:trHeight")
    h.set(qn("w:val"), str(int(cm * 567)))  # 567 twip / cm
    h.set(qn("w:hRule"), rule)
    trPr.append(h)


def remove_table_borders(table):
    tbl = table._tbl
    tblPr = tbl.find(qn("w:tblPr"))
    borders = OxmlElement("w:tblBorders")
    for side in ("top", "left", "bottom", "right", "insideH", "insideV"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "nil")
        borders.append(b)
    tblPr.append(borders)


def set_table_borders(table, *, sz=8, color="000000", inner_sz=4):
    tbl = table._tbl
    tblPr = tbl.find(qn("w:tblPr"))
    borders = OxmlElement("w:tblBorders")
    for side in ("top", "left", "bottom", "right"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "single")
        b.set(qn("w:sz"), str(sz))
        b.set(qn("w:color"), color)
        borders.append(b)
    for side in ("insideH", "insideV"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "single")
        b.set(qn("w:sz"), str(inner_sz))
        b.set(qn("w:color"), color)
        borders.append(b)
    tblPr.append(borders)


def page_break(doc):
    p = doc.add_paragraph()
    p.add_run().add_break(WD_BREAK.PAGE)


# ── 공통 콘텐츠 빌더 ────────────────────────────────────────────────────────
def build_inspection_table(doc, *, header_fill: str, header_color, font="맑은 고딕",
                            zebra: bool = False, border_sz: int = 8):
    headers = ["NO", "분류", "점검 항목", "점검 결과", "특이사항", "확인"]
    widths = [Cm(1.0), Cm(1.8), Cm(6.6), Cm(2.4), Cm(4.5), Cm(1.6)]
    t = doc.add_table(rows=1, cols=len(headers))
    set_table_borders(t, sz=border_sz, inner_sz=border_sz // 2 if border_sz > 4 else 4)

    hdr = t.rows[0]
    for i, h in enumerate(headers):
        c = hdr.cells[i]
        c.width = widths[i]
        shade_cell(c, header_fill)
        c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = c.paragraphs[0].add_run(h)
        set_run(r, font=font, size=10, bold=True, color=header_color)
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    set_row_height(hdr, 0.9)

    for idx, (cat, item, hint) in enumerate(INSPECTION_ITEMS, start=1):
        row = t.add_row()
        cells = row.cells
        cells[0].text = ""; cells[1].text = ""; cells[2].text = ""
        cells[3].text = ""; cells[4].text = ""; cells[5].text = ""
        vals = [str(idx), cat, item, "정상 / 경고 / 오류", "", ""]
        for i, v in enumerate(vals):
            cells[i].width = widths[i]
            cells[i].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            cells[i].paragraphs[0].text = ""
            p = cells[i].paragraphs[0]
            if i in (0, 1, 3, 5):
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            r = p.add_run(v)
            set_run(r, font=font, size=9)
            if i == 2 and hint:
                p2 = cells[i].add_paragraph()
                r2 = p2.add_run(f"  · {hint}")
                set_run(r2, font=font, size=8, color=(0x80, 0x80, 0x80))
        set_row_height(row, 0.8)
        if zebra and idx % 2 == 0:
            for c in cells:
                shade_cell(c, "F5F7FA")
    return t


def build_findings_block(doc, *, header_fill: str, header_color, font="맑은 고딕",
                          border_sz: int = 8):
    add_para(doc, "발견사항 및 조치", font=font, size=12, bold=True)
    headers = ["NO", "발견 일시", "발견 내용", "원인", "조치 내용", "조치 결과"]
    widths = [Cm(1.0), Cm(2.6), Cm(4.6), Cm(3.4), Cm(4.6), Cm(1.8)]
    t = doc.add_table(rows=1, cols=len(headers))
    set_table_borders(t, sz=border_sz, inner_sz=4)
    for i, h in enumerate(headers):
        c = t.rows[0].cells[i]
        c.width = widths[i]
        shade_cell(c, header_fill)
        c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = c.paragraphs[0].add_run(h)
        set_run(r, font=font, size=10, bold=True, color=header_color)
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    set_row_height(t.rows[0], 0.9)
    for i in range(FINDINGS_ROWS):
        row = t.add_row()
        for j, w in enumerate(widths):
            row.cells[j].width = w
            row.cells[j].text = ""
            row.cells[j].paragraphs[0].alignment = (
                WD_ALIGN_PARAGRAPH.CENTER if j in (0, 1, 5) else WD_ALIGN_PARAGRAPH.LEFT
            )
        set_row_height(row, 1.2)
    return t


def build_summary_block(doc, *, font="맑은 고딕", border_sz: int = 8):
    add_para(doc, "종합 의견 및 차회 점검 계획", font=font, size=12, bold=True)
    t = doc.add_table(rows=4, cols=2)
    set_table_borders(t, sz=border_sz, inner_sz=4)
    labels = ["종합 의견", "권고사항", "차회 점검 예정일", "비고"]
    for i, label in enumerate(labels):
        lc = t.rows[i].cells[0]
        lc.width = Cm(3.6)
        shade_cell(lc, "F5F5F5")
        lc.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        lc.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = lc.paragraphs[0].add_run(label)
        set_run(r, font=font, size=10, bold=True)
        rc = t.rows[i].cells[1]
        rc.width = Cm(14.4)
        rc.text = ""
        if label == "종합 의견":
            set_row_height(t.rows[i], 3.2)
        elif label == "권고사항":
            set_row_height(t.rows[i], 2.4)
        else:
            set_row_height(t.rows[i], 1.1)


def build_attach_block(doc, *, header_fill, header_color, font="맑은 고딕", border_sz=8):
    add_para(doc, "첨부", font=font, size=12, bold=True)
    headers = ["NO", "구분", "파일명 / 설명", "비고"]
    widths = [Cm(1.0), Cm(3.0), Cm(11.0), Cm(3.0)]
    t = doc.add_table(rows=1, cols=len(headers))
    set_table_borders(t, sz=border_sz, inner_sz=4)
    for i, h in enumerate(headers):
        c = t.rows[0].cells[i]
        c.width = widths[i]
        shade_cell(c, header_fill)
        c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        r = c.paragraphs[0].add_run(h)
        set_run(r, font=font, size=10, bold=True, color=header_color)
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    set_row_height(t.rows[0], 0.9)
    for i in range(ATTACH_ROWS):
        row = t.add_row()
        for j, w in enumerate(widths):
            row.cells[j].width = w
            row.cells[j].text = ""
        set_row_height(row, 0.9)


# ─────────────────────────────────────────────────────────────────────────────
#                              시안 A — 클래식 격자형
# ─────────────────────────────────────────────────────────────────────────────
def make_classic() -> Path:
    """공공기관 전통 양식 — 굵은 외곽선, 격자 위주, 도장 영역."""
    doc = Document()
    set_page(doc, margin_cm=2.2)
    font = "맑은 고딕"
    HFILL = "E8E8E8"
    HCOLOR = (0, 0, 0)

    # ── 표지 ────────────────────────────────────────────────────────────────
    add_para(doc, "")  # spacer
    add_para(doc, "")  # spacer

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("점  검  내  역  서"), font="바탕", size=32, bold=True)

    add_para(doc, "")
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("INSPECTION  REPORT"), font="Arial", size=12, color=(0x55, 0x55, 0x55))

    add_para(doc, "")
    add_para(doc, "")
    add_para(doc, "")

    # 사업/회차/기관 — 굵은 격자
    cover = doc.add_table(rows=4, cols=2)
    set_table_borders(cover, sz=16, inner_sz=12)
    rows_data = [
        ("사  업  명", COVER_DEFAULTS["사업명"]),
        ("점검 회차 / 월", COVER_DEFAULTS["회차"]),
        ("기  관  명", COVER_DEFAULTS["기관"]),
        ("작  성  일", COVER_DEFAULTS["작성일자"]),
    ]
    for i, (label, val) in enumerate(rows_data):
        lc = cover.rows[i].cells[0]
        lc.width = Cm(4.5)
        shade_cell(lc, "DDDDDD")
        lc.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        lc.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
        set_run(lc.paragraphs[0].add_run(label), font="바탕", size=13, bold=True)
        rc = cover.rows[i].cells[1]
        rc.width = Cm(12.5)
        rc.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        set_run(rc.paragraphs[0].add_run("  " + val), font="바탕", size=12)
        set_row_height(cover.rows[i], 1.4)

    add_para(doc, "")
    add_para(doc, "")
    add_para(doc, "")

    # 확인자 / 점검자 서명란 — 도장 영역 강조
    sig = doc.add_table(rows=4, cols=4)
    set_table_borders(sig, sz=16, inner_sz=12)
    sig_widths = [Cm(2.5), Cm(6.5), Cm(2.5), Cm(5.5)]
    headers_sig = [
        ("구  분", "확  인  자  (담당자 / 공무원·고객)", "구  분", "점  검  자  (수행사)"),
        ("소속·부서", "", "소속·부서", ""),
        ("성      명", "", "성      명", ""),
        ("서      명", "(인)", "서      명", "(인)"),
    ]
    for i, row in enumerate(headers_sig):
        for j, val in enumerate(row):
            c = sig.rows[i].cells[j]
            c.width = sig_widths[j]
            c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            if i == 0 or j % 2 == 0:
                shade_cell(c, "DDDDDD")
                c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
                set_run(c.paragraphs[0].add_run(val), font="바탕", size=11, bold=True)
            else:
                c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.RIGHT if val == "(인)" else WD_ALIGN_PARAGRAPH.LEFT
                set_run(c.paragraphs[0].add_run(val if val else " "), font="바탕", size=11)
            if i == 3:
                set_row_height(sig.rows[i], 2.4)  # 도장 공간
            elif i == 0:
                set_row_height(sig.rows[i], 1.0)
            else:
                set_row_height(sig.rows[i], 1.2)

    # 표지 끝
    page_break(doc)

    # ── 2페이지: 월별 점검사항 ─────────────────────────────────────────────
    add_para(doc, "1. 월별 점검 사항", font="바탕", size=14, bold=True)
    add_para(doc, "  ※ 행은 사이트·월별로 자유롭게 추가·삭제·수정 가능. 점검 결과 칸은 정상/경고/오류 중 택1.", font=font, size=9, color=(0x66, 0x66, 0x66))
    add_para(doc, "")
    build_inspection_table(doc, header_fill=HFILL, header_color=HCOLOR, font=font, border_sz=12)

    page_break(doc)

    # ── 3페이지: 발견사항·조치 + 종합의견 ──────────────────────────────────
    add_para(doc, "2. 발견사항 및 조치", font="바탕", size=14, bold=True)
    add_para(doc, "")
    build_findings_block(doc, header_fill=HFILL, header_color=HCOLOR, font=font, border_sz=12)

    add_para(doc, "")
    add_para(doc, "3. 종합 의견 및 차회 점검 계획", font="바탕", size=14, bold=True)
    add_para(doc, "")
    build_summary_block(doc, font=font, border_sz=12)

    add_para(doc, "")
    add_para(doc, "4. 첨부", font="바탕", size=14, bold=True)
    add_para(doc, "")
    build_attach_block(doc, header_fill=HFILL, header_color=HCOLOR, font=font, border_sz=12)

    path = OUT_DIR / "점검내역서_시안A_classic.docx"
    doc.save(path)
    return path


# ─────────────────────────────────────────────────────────────────────────────
#                              시안 B — 모던 헤더형
# ─────────────────────────────────────────────────────────────────────────────
def make_modern() -> Path:
    """진한 컬러 배너 헤더, 큰 폰트, 넉넉한 여백, 카드형 레이아웃."""
    doc = Document()
    set_page(doc, margin_cm=1.8)
    font = "맑은 고딕"
    BRAND = "1F4E78"            # 진한 청색
    BRAND_LIGHT = "DCE6F1"
    HCOLOR = (0xFF, 0xFF, 0xFF)
    BRAND_RGB = (0x1F, 0x4E, 0x78)

    # 컬러 배너
    banner = doc.add_table(rows=1, cols=1)
    bc = banner.rows[0].cells[0]
    bc.width = Cm(17.4)
    shade_cell(bc, BRAND)
    bc.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    bc.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(bc.paragraphs[0].add_run("점검내역서"),
            font=font, size=36, bold=True, color=(0xFF, 0xFF, 0xFF))
    set_row_height(banner.rows[0], 4.0)
    remove_table_borders(banner)

    add_para(doc, "")
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    set_run(p.add_run("Monthly Inspection Report"),
            font="Arial", size=11, color=(0x80, 0x80, 0x80))

    add_para(doc, "")
    add_para(doc, "")

    # 메타 정보 — 카드 그리드 (2 × 2)
    meta = doc.add_table(rows=2, cols=2)
    set_table_borders(meta, sz=4, color="D9D9D9", inner_sz=4)
    meta_items = [
        ("사업명", COVER_DEFAULTS["사업명"]),
        ("점검 회차", COVER_DEFAULTS["회차"]),
        ("기관 / 발주처", COVER_DEFAULTS["기관"]),
        ("작성일", COVER_DEFAULTS["작성일자"]),
    ]
    for idx, (label, val) in enumerate(meta_items):
        r, c = divmod(idx, 2)
        cell = meta.rows[r].cells[c]
        cell.width = Cm(8.6)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.TOP
        p1 = cell.paragraphs[0]
        set_run(p1.add_run(label.upper()),
                font=font, size=8, bold=True, color=(0x80, 0x80, 0x80))
        p2 = cell.add_paragraph()
        set_run(p2.add_run(val), font=font, size=14, bold=True, color=BRAND_RGB)
        set_row_height(meta.rows[r], 2.4)

    add_para(doc, "")
    add_para(doc, "")

    # 확인자 / 점검자 — 모던 2분할
    sig_label = doc.add_paragraph()
    set_run(sig_label.add_run("확인자 / 점검자"),
            font=font, size=11, bold=True, color=(0x80, 0x80, 0x80))

    sig = doc.add_table(rows=4, cols=4)
    set_table_borders(sig, sz=4, color="D9D9D9", inner_sz=4)
    sig_widths = [Cm(2.4), Cm(6.4), Cm(2.4), Cm(6.2)]
    sig_data = [
        ("구분", "확인자 (담당자·공무원/고객)", "구분", "점검자 (수행사)"),
        ("소속", "", "소속", ""),
        ("성명", "", "성명", ""),
        ("서명", "", "서명", ""),
    ]
    for i, row in enumerate(sig_data):
        for j, val in enumerate(row):
            cell = sig.rows[i].cells[j]
            cell.width = sig_widths[j]
            cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            if i == 0:
                shade_cell(cell, BRAND)
                cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
                set_run(cell.paragraphs[0].add_run(val),
                        font=font, size=11, bold=True, color=(0xFF, 0xFF, 0xFF))
            elif j % 2 == 0:
                shade_cell(cell, BRAND_LIGHT)
                cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
                set_run(cell.paragraphs[0].add_run(val), font=font, size=11, bold=True, color=BRAND_RGB)
            else:
                set_run(cell.paragraphs[0].add_run(" "), font=font, size=11)
            if i == 0:
                set_row_height(sig.rows[i], 1.0)
            elif i == 3:
                set_row_height(sig.rows[i], 2.2)
            else:
                set_row_height(sig.rows[i], 1.2)

    page_break(doc)

    # 2페이지: 월별 점검사항
    title = doc.add_paragraph()
    set_run(title.add_run("01"),
            font=font, size=20, bold=True, color=BRAND_RGB)
    set_run(title.add_run("   월별 점검 사항"),
            font=font, size=16, bold=True)
    add_para(doc, "  · 사이트/월별로 행 추가·삭제·항목 수정 가능. 결과 칸은 정상/경고/오류 택1.",
             font=font, size=9, color=(0x80, 0x80, 0x80))
    add_para(doc, "")
    build_inspection_table(doc, header_fill=BRAND, header_color=HCOLOR, font=font, zebra=True, border_sz=4)

    page_break(doc)

    # 3페이지: 발견사항·조치 + 종합의견 + 첨부
    title = doc.add_paragraph()
    set_run(title.add_run("02"), font=font, size=20, bold=True, color=BRAND_RGB)
    set_run(title.add_run("   발견사항 및 조치"), font=font, size=16, bold=True)
    add_para(doc, "")
    build_findings_block(doc, header_fill=BRAND, header_color=HCOLOR, font=font, border_sz=4)

    add_para(doc, "")
    title = doc.add_paragraph()
    set_run(title.add_run("03"), font=font, size=20, bold=True, color=BRAND_RGB)
    set_run(title.add_run("   종합 의견 및 차회 점검 계획"), font=font, size=16, bold=True)
    add_para(doc, "")
    build_summary_block(doc, font=font, border_sz=4)

    add_para(doc, "")
    title = doc.add_paragraph()
    set_run(title.add_run("04"), font=font, size=20, bold=True, color=BRAND_RGB)
    set_run(title.add_run("   첨부"), font=font, size=16, bold=True)
    add_para(doc, "")
    build_attach_block(doc, header_fill=BRAND, header_color=HCOLOR, font=font, border_sz=4)

    path = OUT_DIR / "점검내역서_시안B_modern.docx"
    doc.save(path)
    return path


# ─────────────────────────────────────────────────────────────────────────────
#                       시안 C v2 — 미니멀 모던 (피드백 반영)
# ─────────────────────────────────────────────────────────────────────────────
# 변경점 (사용자 피드백):
#   1) 점검 이력 표 추가 — 이번 달이 2월이면 1월 회차가 그 위에 누적 표시
#   2) 점검사항 표 4컬럼 (NO/분류/항목/결과) — 특이사항·확인 컬럼 제거
#   3) 특이사항을 별도 자유 기술 박스로 분리
#   4) 발견사항·조치, 종합의견·차회 점검, 첨부 섹션 모두 제거
#   5) 디자인 톤 — 다크 헤로 + 가는 액센트 라인, 큰 헤딩, 표 외곽 무선·헤더만 컬러

DARK_HEX = "0F172A"          # slate-900
DARK_RGB = (0x0F, 0x17, 0x2A)
ACCENT_HEX_V2 = "10B981"     # emerald-500
ACCENT_RGB_V2 = (0x10, 0xB9, 0x81)
MUTED_RGB = (0x6B, 0x72, 0x80)
LIGHT_BG = "F8FAFC"          # slate-50
DIV_HEX = "E2E8F0"           # slate-200

HISTORY_ROWS_DEFAULT = 6     # 이전 회차 + 이번 회차 포함 누적 행
INSPECTION_ITEMS_V2 = INSPECTION_ITEMS   # 동일 데이터, 컬럼만 축소


def _section_heading(doc, eyebrow: str, title_kr: str, font: str):
    """모던 섹션 헤더 — 작은 영문 eyebrow 위, 큰 한글 타이틀 아래, 가는 액센트 라인."""
    p = doc.add_paragraph()
    set_run(p.add_run(eyebrow.upper()),
            font="Arial", size=8, bold=True, color=ACCENT_RGB_V2)
    p = doc.add_paragraph()
    set_run(p.add_run(title_kr), font=font, size=20, bold=True, color=DARK_RGB)
    # 가는 가로 액센트 라인 (1행 1셀 미니 테이블)
    line = doc.add_table(rows=1, cols=1)
    remove_table_borders(line)
    c = line.rows[0].cells[0]
    c.width = Cm(2.4)
    shade_cell(c, ACCENT_HEX_V2)
    set_row_height(line.rows[0], 0.08, rule="exact")
    c.paragraphs[0].text = ""
    add_para(doc, "")


def _modern_inspection_table(doc, font: str):
    """4컬럼 (NO / 분류 / 점검항목 / 결과) — 외곽 무선, 헤더만 다크, zebra."""
    headers = ["NO", "분류", "점검 항목", "결과"]
    widths = [Cm(1.2), Cm(2.6), Cm(10.6), Cm(3.0)]
    t = doc.add_table(rows=1, cols=len(headers))
    # 외곽 무선, 헤더 하단·내부 가로선만
    tbl = t._tbl
    tblPr = tbl.find(qn("w:tblPr"))
    borders = OxmlElement("w:tblBorders")
    for side in ("top", "left", "right", "insideV"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "nil")
        borders.append(b)
    for side in ("bottom", "insideH"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "single")
        b.set(qn("w:sz"), "4")
        b.set(qn("w:color"), DIV_HEX)
        borders.append(b)
    tblPr.append(borders)

    hdr = t.rows[0]
    for i, h in enumerate(headers):
        c = hdr.cells[i]
        c.width = widths[i]
        shade_cell(c, DARK_HEX)
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER if i in (0, 1, 3) else WD_ALIGN_PARAGRAPH.LEFT
        # left-align 항목 컬럼은 들여쓰기
        text = ("  " + h) if i == 2 else h
        set_run(c.paragraphs[0].add_run(text),
                font=font, size=10, bold=True, color=(0xFF, 0xFF, 0xFF))
    set_row_height(hdr, 0.95)

    for idx, (cat, item, hint) in enumerate(INSPECTION_ITEMS_V2, start=1):
        row = t.add_row()
        cells = row.cells
        vals = [str(idx), cat, item, "정상 / 경고 / 오류"]
        for i, v in enumerate(vals):
            cells[i].width = widths[i]
            cells[i].vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            cells[i].text = ""
            p = cells[i].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if i in (0, 1, 3) else WD_ALIGN_PARAGRAPH.LEFT
            text = ("  " + v) if i == 2 else v
            set_run(p.add_run(text), font=font, size=10, color=DARK_RGB if i != 3 else MUTED_RGB)
            if i == 2 and hint:
                p2 = cells[i].add_paragraph()
                set_run(p2.add_run("  · " + hint),
                        font=font, size=8, color=MUTED_RGB)
        set_row_height(row, 0.82)
        if idx % 2 == 0:
            for c in cells:
                shade_cell(c, LIGHT_BG)


def _history_table(doc, font: str):
    """점검 이력 — 회차/일자/점검자/요약. 이번 달이 2월이면 1월 회차가 위에."""
    headers = ["회차", "점검 일자", "점검자", "확인자", "주요 결과 요약"]
    widths = [Cm(1.5), Cm(2.6), Cm(2.6), Cm(2.6), Cm(8.1)]
    t = doc.add_table(rows=1, cols=len(headers))
    # 헤더 하단·행 사이 얇은 가로선만, 외곽 무선
    tbl = t._tbl
    tblPr = tbl.find(qn("w:tblPr"))
    borders = OxmlElement("w:tblBorders")
    for side in ("top", "left", "right", "insideV"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "nil")
        borders.append(b)
    for side in ("bottom", "insideH"):
        b = OxmlElement(f"w:{side}")
        b.set(qn("w:val"), "single")
        b.set(qn("w:sz"), "4")
        b.set(qn("w:color"), DIV_HEX)
        borders.append(b)
    tblPr.append(borders)

    hdr = t.rows[0]
    for i, h in enumerate(headers):
        c = hdr.cells[i]
        c.width = widths[i]
        c.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.LEFT if i == 4 else WD_ALIGN_PARAGRAPH.CENTER
        text = ("  " + h) if i == 4 else h
        set_run(c.paragraphs[0].add_run(text),
                font=font, size=9, bold=True, color=MUTED_RGB)
    set_row_height(hdr, 0.8)

    for i in range(HISTORY_ROWS_DEFAULT):
        row = t.add_row()
        for j, w in enumerate(widths):
            row.cells[j].width = w
            row.cells[j].text = ""
            p = row.cells[j].paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT if j == 4 else WD_ALIGN_PARAGRAPH.CENTER
            # 가장 최근(이번 회차) 행을 마지막 행으로 강조 표시
            if i == HISTORY_ROWS_DEFAULT - 1:
                shade_cell(row.cells[j], LIGHT_BG)
                placeholder = ["이번 회차", "", "", "", "  ← 본 점검내역서 (작성 중)"][j]
                set_run(p.add_run(placeholder),
                        font=font, size=10, bold=(j == 0), color=ACCENT_RGB_V2 if j == 0 else MUTED_RGB)
            else:
                placeholder_map = {
                    0: f"제 {HISTORY_ROWS_DEFAULT - i - 1} 회차",
                    1: "20  -  -  ",
                    2: "",
                    3: "",
                    4: "",
                }
                set_run(p.add_run(placeholder_map[j]),
                        font=font, size=10, color=MUTED_RGB if j < 4 else DARK_RGB)
        set_row_height(row, 1.0)


def _notes_box(doc, font: str, *, lines: int = 8):
    """특이사항 자유 기술 박스 — 큰 빈 영역, 얇은 회색 외곽."""
    t = doc.add_table(rows=1, cols=1)
    set_table_borders(t, sz=4, color=DIV_HEX[2:] if DIV_HEX.startswith("0x") else DIV_HEX, inner_sz=4)
    c = t.rows[0].cells[0]
    c.width = Cm(17.4)
    set_row_height(t.rows[0], lines * 0.7)
    c.text = ""
    p = c.paragraphs[0]
    set_run(p.add_run("  여기에 자유 기술 (사이트 특이상황·교체 부품·일정 변경·기타 코멘트 등)"),
            font=font, size=9, color=(0xC0, 0xC0, 0xC0))


def make_minimal() -> Path:
    doc = Document()
    set_page(doc, margin_cm=2.0)
    font = "맑은 고딕"

    # ── 표지 ────────────────────────────────────────────────────────────────
    # 다크 헤로 블록 (페이지 상단 1/3)
    hero = doc.add_table(rows=1, cols=1)
    remove_table_borders(hero)
    hc = hero.rows[0].cells[0]
    hc.width = Cm(17.0)
    shade_cell(hc, DARK_HEX)
    hc.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
    set_row_height(hero.rows[0], 7.0)

    # eyebrow
    p = hc.paragraphs[0]
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    set_run(p.add_run("    INSPECTION  REPORT"),
            font="Arial", size=9, bold=True, color=ACCENT_RGB_V2)
    # 큰 한글 타이틀
    p = hc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    set_run(p.add_run("    점검내역서"),
            font=font, size=44, bold=True, color=(0xFF, 0xFF, 0xFF))
    # 부제
    p = hc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    set_run(p.add_run("    월간 운영·유지관리 점검 보고"),
            font=font, size=12, color=(0xCB, 0xD5, 0xE1))

    # 액센트 가는 라인 (헤로 하단 강조)
    line = doc.add_table(rows=1, cols=1)
    remove_table_borders(line)
    lc = line.rows[0].cells[0]
    lc.width = Cm(2.4)
    shade_cell(lc, ACCENT_HEX_V2)
    set_row_height(line.rows[0], 0.08, rule="exact")
    lc.paragraphs[0].text = ""

    add_para(doc, "")
    add_para(doc, "")

    # 메타 정보 — 라벨(작은 회색) + 값(큰 진한) 카드 그리드 2x2
    meta = doc.add_table(rows=2, cols=2)
    set_table_borders(meta, sz=4, color=DIV_HEX, inner_sz=4)
    meta_items = [
        ("PROJECT", "사업명", COVER_DEFAULTS["사업명"]),
        ("PERIOD", "점검 회차 · 월", COVER_DEFAULTS["회차"]),
        ("CLIENT", "기관 / 발주처", COVER_DEFAULTS["기관"]),
        ("ISSUED", "작성일자", COVER_DEFAULTS["작성일자"]),
    ]
    for idx, (eyebrow, label, val) in enumerate(meta_items):
        r, c = divmod(idx, 2)
        cell = meta.rows[r].cells[c]
        cell.width = Cm(8.5)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.TOP
        # 셀 내부 여백을 위한 빈 paragraph 제거하고 새로 구성
        cell.text = ""
        p1 = cell.paragraphs[0]
        set_run(p1.add_run("  " + eyebrow),
                font="Arial", size=8, bold=True, color=ACCENT_RGB_V2)
        p2 = cell.add_paragraph()
        set_run(p2.add_run("  " + label),
                font=font, size=8, color=MUTED_RGB)
        p3 = cell.add_paragraph()
        set_run(p3.add_run("  " + val),
                font=font, size=13, bold=True, color=DARK_RGB)
        set_row_height(meta.rows[r], 2.8)

    add_para(doc, "")
    add_para(doc, "")

    # 확인자 / 점검자 — 미니 eyebrow + 2분할 카드 (소속/성명/서명만)
    p = doc.add_paragraph()
    set_run(p.add_run("CONFIRMED  &  INSPECTED  BY"),
            font="Arial", size=8, bold=True, color=ACCENT_RGB_V2)
    p = doc.add_paragraph()
    set_run(p.add_run("확인자 · 점검자"),
            font=font, size=12, bold=True, color=DARK_RGB)
    add_para(doc, "")

    sig = doc.add_table(rows=4, cols=4)
    set_table_borders(sig, sz=4, color=DIV_HEX, inner_sz=4)
    sig_widths = [Cm(1.8), Cm(6.6), Cm(1.8), Cm(7.0)]
    sig_data = [
        ("구분", "확인자 (담당자·공무원/고객)", "구분", "점검자 (수행사)"),
        ("소속", "", "소속", ""),
        ("성명", "", "성명", ""),
        ("서명", "", "서명", ""),
    ]
    for i, row in enumerate(sig_data):
        for j, val in enumerate(row):
            cell = sig.rows[i].cells[j]
            cell.width = sig_widths[j]
            cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
            if i == 0:
                shade_cell(cell, DARK_HEX)
                cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
                set_run(cell.paragraphs[0].add_run(val),
                        font=font, size=10, bold=True, color=(0xFF, 0xFF, 0xFF))
            elif j % 2 == 0:
                shade_cell(cell, LIGHT_BG)
                cell.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
                set_run(cell.paragraphs[0].add_run(val),
                        font=font, size=10, bold=True, color=MUTED_RGB)
            else:
                set_run(cell.paragraphs[0].add_run(" "), font=font, size=11)
            if i == 0:
                set_row_height(sig.rows[i], 0.95)
            elif i == 3:
                set_row_height(sig.rows[i], 2.4)
            else:
                set_row_height(sig.rows[i], 1.2)

    page_break(doc)

    # ── 2페이지: 점검 이력 (Inspection History) ─────────────────────────────
    _section_heading(doc, "01 · history", "점검 이력", font)
    add_para(doc, "  · 가장 최근 회차(이번 달)가 표 맨 아래에 위치. 이전 회차의 점검 일자·점검자·요약을 누적 기록.",
             font=font, size=9, color=MUTED_RGB)
    add_para(doc, "")
    _history_table(doc, font)

    add_para(doc, "")
    add_para(doc, "")

    # ── 2페이지 하단 또는 페이지 분기: 이번 달 점검 사항 ─────────────────────
    _section_heading(doc, "02 · this month", "이번 달 점검 사항", font)
    add_para(doc, "  · 결과 칸은 정상 / 경고 / 오류 중 택1. 행은 사이트·월별로 자유롭게 추가·삭제·항목 수정 가능.",
             font=font, size=9, color=MUTED_RGB)
    add_para(doc, "")
    _modern_inspection_table(doc, font)

    page_break(doc)

    # ── 3페이지: 특이사항 (자유 기술) ────────────────────────────────────────
    _section_heading(doc, "03 · notes", "특이사항", font)
    add_para(doc, "  · 점검 항목 표로 표현되지 않는 자유 코멘트 (사이트 환경, 일정 변경, 교체 부품, 후속 조치 메모 등).",
             font=font, size=9, color=MUTED_RGB)
    add_para(doc, "")
    _notes_box(doc, font, lines=14)

    path = OUT_DIR / "점검내역서_시안C_minimal_v2.docx"
    doc.save(path)
    return path


# ─────────────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    import sys
    # PowerShell cp949 회피 — stdout 강제 UTF-8
    try:
        sys.stdout.reconfigure(encoding="utf-8")
    except Exception:
        pass

    results = [
        ("A (classic grid)", make_classic()),
        ("B (modern header)", make_modern()),
        ("C (minimal line)", make_minimal()),
    ]
    print("-" * 60)
    for label, path in results:
        size = path.stat().st_size
        print(f"  [OK] draft {label:20s} -> {path.name}  ({size:,} B)")
    print("-" * 60)
    print(f"output dir: {OUT_DIR}")
