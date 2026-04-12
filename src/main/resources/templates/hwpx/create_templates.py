#!/usr/bin/env python3
"""
Create HWPX template files by copying source HWPX files and replacing
actual data values with {{placeholder}} variables in section0.xml.
"""

import zipfile
import os
import sys

sys.stdout.reconfigure(encoding='utf-8')

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))

# Template definitions: (output_name, source_path, replacements_list)
# Replacements are applied in order. Each is (old_string, new_string).
# Order matters: longer/more specific strings should be replaced first to avoid partial matches.

TEMPLATES = [
    {
        "name": "letter_commence.hwpx",
        "source": r"C:\Users\PUJ\직무\착수계\KRAS\가평군(우편발송)\3. 착수\1. 착수계 공문_가평군_2026년도 KRAS 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역.hwpx",
        "replacements": [
            # 제목 (split across two <hp:t> tags, handle the main part first)
            ("「2026년도 KRAS 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역", "「{{제목_용역명}}"),
            ("」착수계 제출 건", "」{{제목_접미사}}"),
            # 용역명 in body (inside 『...』)
            ("2026년도 KRAS 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역", "{{용역명}}"),
            # 수신
            ("가평군청", "{{수신}}"),
            # 담당
            ("이사 박욱진", "{{담당}}"),
            # 연락처
            ("070-7113-8093", "{{연락처}}"),
            # 문서번호 (split: "(주)정도UIT 2025 –" and "912호")
            ("(주)정도UIT 2025 –", "{{문서번호_앞}}"),
            ("912호", "{{문서번호_뒤}}"),
            # 시행일자
            ("2025. 12.   .", "{{시행일자}}"),
            # 계약일자
            ("2025. 12. 30.", "{{계약일자}}"),
        ]
    },
    {
        "name": "letter_interim.hwpx",
        "source": r"C:\Users\PUJ\직무\기성계\강원도 강릉시\1.기성신청서(공문)_강릉시_부동산종합공부시스템 국산 공간정보 SW 유지보수 용역.hwpx",
        "replacements": [
            # 제목 (split across tags)
            ("「부동산종합공부시스템 국산 공간정보 SW 유지보수 용역", "「{{제목_용역명}}"),
            ("」기성금 신청 건", "」{{제목_접미사}}"),
            # 용역명 in body
            ("부동산종합공부시스템 국산 공간정보 SW 유지보수 용역", "{{용역명}}"),
            # 수신
            ("강릉시청", "{{수신}}"),
            # 담당
            ("이사 박욱진", "{{담당}}"),
            # 연락처
            ("070-7113-8093", "{{연락처}}"),
            # 문서번호
            ("(주)정도UIT 2025 – 415 호", "{{문서번호}}"),
            # 시행일자
            ("2025.   6.    .", "{{시행일자}}"),
            # 계약일자
            ("24.12.31", "{{계약일자}}"),
        ]
    },
    {
        "name": "letter_completion.hwpx",
        "source": r"C:\Users\PUJ\직무\준공계\김한준_경기도 가평군_KRAS\1. 준공계 공문_가평군_2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역.hwpx",
        "replacements": [
            # 제목 (split across tags)
            ("「2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역", "「{{제목_용역명}}"),
            ("」준공계 제출 건", "」{{제목_접미사}}"),
            # 용역명 in body
            ("2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역", "{{용역명}}"),
            # 수신
            ("가평군청", "{{수신}}"),
            # 담당
            ("이사 박욱진", "{{담당}}"),
            # 연락처
            ("070-7113-8093", "{{연락처}}"),
            # 문서번호 (split: "(주)정도UIT 2025" and "– 706호")
            ("(주)정도UIT 2025", "{{문서번호_앞}}"),
            ("– 706호", "{{문서번호_뒤}}"),
            # 시행일자
            ("2025. 12.   .", "{{시행일자}}"),
            # 계약일자
            ("2024. 12. 31.", "{{계약일자}}"),
        ]
    },
    {
        "name": "interim_inspector.hwpx",
        "source": r"C:\Users\PUJ\직무\기성계\강원도 강릉시\2.기성신청서(기성검사원)_강릉시_부동산종합공부시스템 국산 공간정보 SW 유지보수 용역.hwpx",
        "replacements": [
            # 용역명 (appears multiple times - replace ALL)
            ("부동산종합공부시스템 국산 공간정보 SW 유지보수 용역", "{{용역명}}"),
            # 수신
            ("강릉시장 귀하", "{{수신}}"),
            # 계약금액 with Korean (combined in one tag)
            ("금8,670,000원(금팔백육십칠만원)", "{{계약금액}}({{계약금액한글}})"),
            # 금회 기성 금액
            ("금4,335,000원(금사백삼십삼만오천원)", "{{기성금액}}({{기성금액한글}})"),
            # 계약년월일 parts (separate tags: "2024년", "12월", "31일")
            ("2024년", "{{계약년도}}"),
            ("01월", "{{착수월}}"),
            ("12월", "{{계약월}}"),
            ("31일", "{{계약일}}"),
            # 기성 날짜
            ("2025년    06월    일", "{{기성일자}}"),
        ]
    },
    {
        "name": "completion_kras.hwpx",
        "source": r"C:\Users\PUJ\직무\준공계\김한준_경기도 가평군_KRAS\2. 준공계_가평군_2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역.hwpx",
        "replacements": [
            # 점검내역 title (longest match first - contains 『...』)
            ("『2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS) 유지보수 용역』점검내역", "『{{용역명}}』점검내역"),
            # 용역명 (appears multiple times, some split across tags)
            # The split version: "2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS)" + "유지보수 용역"
            ("2025년도 부동산종합공부시스템 국산 공간정보 SW(GeoNURIS for KRAS)", "{{용역명_앞}}"),
            ("유지보수 용역", "{{용역명_뒤}}"),
            # 수신
            ("가평군수 귀하", "{{수신}}"),
            # 계약금액
            ("금8,778,000원(금팔백칠십칠만팔천원)", "{{계약금액}}({{계약금액한글}})"),
            # 착수기간 (MUST come before individual date replacements)
            ("2025년 01월 01일 ~ 2025년 12월 31일", "{{착수일}} ~ {{준공예정일2}}"),
            # 계약일자
            ("2024년 12월 31일", "{{계약일자}}"),
            # 준공예정일
            ("2025년 12월 31일", "{{준공예정일}}"),
            # 실제준공일 and 제출일
            ("2025년 12월   일", "{{준공일}}"),
            # 점검 날짜들
            ("2025년", "{{점검년도}}"),
        ]
    },
]


def create_template(template_def):
    """Create a single HWPX template by copying source and replacing text in section0.xml."""
    name = template_def["name"]
    source = template_def["source"]
    replacements = template_def["replacements"]
    output_path = os.path.join(OUTPUT_DIR, name)

    print(f"\n{'='*60}")
    print(f"Creating template: {name}")
    print(f"Source: {source}")

    if not os.path.exists(source):
        print(f"  ERROR: Source file not found!")
        return False

    # Read source HWPX (it's a ZIP file)
    with zipfile.ZipFile(source, 'r') as src_zip:
        file_list = src_zip.namelist()
        print(f"  Files in source: {len(file_list)}")

        # Read section0.xml
        section_content = src_zip.read('Contents/section0.xml').decode('utf-8')

        # Apply replacements
        print(f"  Applying {len(replacements)} replacements:")
        for old_str, new_str in replacements:
            count = section_content.count(old_str)
            if count > 0:
                section_content = section_content.replace(old_str, new_str)
                print(f"    OK: \"{old_str}\" -> \"{new_str}\" ({count} occurrences)")
            else:
                print(f"    WARN: \"{old_str}\" NOT FOUND in section0.xml")

        # Write new HWPX
        with zipfile.ZipFile(output_path, 'w', zipfile.ZIP_DEFLATED) as dst_zip:
            for file_name in file_list:
                if file_name == 'Contents/section0.xml':
                    # Write modified section0.xml
                    dst_zip.writestr(file_name, section_content.encode('utf-8'))
                else:
                    # Copy as-is
                    data = src_zip.read(file_name)
                    dst_zip.writestr(file_name, data)

    print(f"  Output: {output_path}")
    return True


def verify_template(template_name):
    """Verify a template by extracting section0.xml and listing all {{...}} placeholders."""
    path = os.path.join(OUTPUT_DIR, template_name)
    if not os.path.exists(path):
        print(f"  Template not found: {path}")
        return

    import re
    with zipfile.ZipFile(path, 'r') as z:
        content = z.read('Contents/section0.xml').decode('utf-8')
        placeholders = re.findall(r'\{\{[^}]+\}\}', content)
        unique_placeholders = sorted(set(placeholders))
        print(f"\n  Placeholders in {template_name}:")
        for p in unique_placeholders:
            count = placeholders.count(p)
            suffix = f" (x{count})" if count > 1 else ""
            print(f"    {p}{suffix}")
        print(f"  Total: {len(unique_placeholders)} unique placeholders, {len(placeholders)} total occurrences")


def main():
    print(f"Output directory: {OUTPUT_DIR}")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    success_count = 0
    for template_def in TEMPLATES:
        if create_template(template_def):
            success_count += 1

    print(f"\n{'='*60}")
    print(f"Created {success_count}/{len(TEMPLATES)} templates")

    # Verification
    print(f"\n{'='*60}")
    print("VERIFICATION")
    print(f"{'='*60}")
    for template_def in TEMPLATES:
        verify_template(template_def["name"])

    # List output files
    print(f"\n{'='*60}")
    print("Output files:")
    for f in sorted(os.listdir(OUTPUT_DIR)):
        if f.endswith('.hwpx'):
            size = os.path.getsize(os.path.join(OUTPUT_DIR, f))
            print(f"  {f} ({size:,} bytes)")


if __name__ == '__main__':
    main()
