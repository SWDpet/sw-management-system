#!/usr/bin/env python3
"""
docs-renewal-01 P4: 링크 일괄 치환 스크립트

대상: **/*.md, **/*.html, src/main/java/**/*.java, src/main/resources/**/*.sql,
      src/main/resources/**/*.properties, src/main/resources/**/*.yml,
      src/main/resources/static/js/**/*.js

치환 규칙: `REPLACE_RULES` (우선순위 순).
"""
from pathlib import Path
import re
import sys

REPO = Path(__file__).resolve().parent.parent.parent.parent

# 우선순위 순 (특정 > 일반)
REPLACE_RULES = [
    # 개별 파일 rename (구체적 경로 먼저)
    ("DEPLOYMENT_GUIDE.md",          "docs/exec-plans/archive/quotation-deploy.md"),
    ("DEVELOPMENT_GUIDELINES.md",    "AGENTS.md"),
    ("docs/견적서_VAT_표시기준.md",   "docs/product-specs/quotation-vat-rules.md"),
    ("docs/AI_SEARCH_PLAN.md",        "docs/product-specs/ai-search.md"),
    ("docs/OBSIDIAN_SETUP.md",        "docs/references/obsidian-setup.md"),
    ("docs/SETUP_GUIDE.md",           "docs/references/setup-guide.md"),
    ("docs/ERD.md",                   "docs/generated/erd.md"),
    ("docs/DESIGN_SYSTEM.md",         "docs/DESIGN.md"),
    ("docs/HOME.md",                  "docs/PRODUCT_SENSE.md"),
    ("docs/TEAM_WORKFLOW.md",         "AGENTS.md"),

    # design-docs 선별 5건
    ("docs/plans/pjt-equip-decision.md",       "docs/design-docs/pjt-equip-decision.md"),
    ("docs/plans/tb-work-plan-decision.md",    "docs/design-docs/tb-work-plan-decision.md"),
    ("docs/plans/data-architecture-roadmap.md","docs/design-docs/data-architecture-roadmap.md"),
    ("docs/plans/qt-remarks-users-link.md",    "docs/design-docs/qt-remarks-users-link.md"),
    ("docs/plans/doc-structure-renewal.md",    "docs/design-docs/doc-structure-renewal.md"),

    # 일반 폴더 경로 (뒤에)
    ("docs/dev-plans/", "docs/exec-plans/"),
    ("docs/audit/",     "docs/generated/audit/"),
    ("docs/plans/",     "docs/product-specs/"),
]

TARGET_GLOBS = [
    "**/*.md",
    "**/*.html",
    "**/*.java",        # src/main/java + docs/exec-plans + docs/generated/audit 내 .java 포함
    "**/*.sql",         # src/main/resources + swdept/sql
    "**/*.properties",
    "**/*.yml",         # docs/erd-descriptions.yml 등 포함
    "**/*.yaml",
    "src/main/resources/static/js/**/*.js",
]

EXCLUDE_DIRS = {
    ".git", "target", "node_modules", ".idea", ".vscode",
    "docs/.obsidian",
    # 스크립트 자체는 치환 대상 아님
    "docs/exec-plans/docs-renewal-tools",
}


def is_excluded(path: Path) -> bool:
    rel = path.relative_to(REPO).as_posix()
    for ex in EXCLUDE_DIRS:
        if rel.startswith(ex + "/") or rel == ex:
            return True
    return False


def collect_files():
    files = set()
    for pat in TARGET_GLOBS:
        for f in REPO.glob(pat):
            if f.is_file() and not is_excluded(f):
                files.add(f)
    return sorted(files)


def apply_replacements(content: str) -> tuple[str, int]:
    total_hits = 0
    for old, new in REPLACE_RULES:
        count = content.count(old)
        if count > 0:
            content = content.replace(old, new)
            total_hits += count
    return content, total_hits


def main():
    files = collect_files()
    print(f"[INFO] 대상 파일 {len(files)}개 스캔")

    total_replaced_files = 0
    total_hits = 0
    for f in files:
        try:
            original = f.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        new_content, hits = apply_replacements(original)
        if hits > 0:
            f.write_text(new_content, encoding="utf-8")
            rel = f.relative_to(REPO).as_posix()
            print(f"  {rel}: {hits}개 치환")
            total_replaced_files += 1
            total_hits += hits

    print(f"\n[DONE] {total_replaced_files} files / {total_hits} hits replaced")


if __name__ == "__main__":
    main()
