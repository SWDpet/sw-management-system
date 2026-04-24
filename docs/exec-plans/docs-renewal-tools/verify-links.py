#!/usr/bin/env python3
"""docs-renewal-01 NFR-4: 링크 검증 스크립트.

Markdown `[text](path)` 링크 + HTML `href=`/`src=` 대상 파일 존재 확인.
외부 링크(http/https/mailto)는 제외. #anchor 는 대상 md 내부 heading slug 일치 확인.
exit 0 = PASS, 1 = FAIL.
"""
from __future__ import annotations
import re
import sys
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent.parent.parent

EXCLUDE = {
    ".git", "target", "node_modules", ".idea", ".vscode",
    "docs/.obsidian", "docs/exec-plans/docs-renewal-tools",
}

MD_LINK = re.compile(r'\[(?P<text>[^\]]*)\]\((?P<url>[^)]+)\)')
HTML_ATTR = re.compile(r'(?:href|src)="(?P<url>[^"]+)"')


def is_excluded(path: Path) -> bool:
    try:
        rel = path.relative_to(REPO).as_posix()
    except ValueError:
        return True
    for ex in EXCLUDE:
        if rel.startswith(ex + "/") or rel == ex:
            return True
    return False


def slugify(heading: str) -> str:
    """GitHub 스타일 slug 근사: 소문자 + 공백→하이픈 + 영숫자/하이픈/한글 유지."""
    h = heading.strip().lower()
    h = re.sub(r'[^\w\s\-가-힣]', '', h)
    h = re.sub(r'\s+', '-', h)
    return h


def collect_headings(md_path: Path) -> set[str]:
    slugs = set()
    try:
        for line in md_path.read_text(encoding='utf-8').splitlines():
            m = re.match(r'^\s{0,3}(#{1,6})\s+(.+?)\s*$', line)
            if m:
                slugs.add(slugify(m.group(2)))
    except Exception:
        pass
    return slugs


def resolve_link(base: Path, raw: str) -> tuple[Path, str]:
    """링크에서 앵커 분리 + 상대경로 → 절대경로 변환."""
    if '#' in raw:
        path_part, anchor = raw.split('#', 1)
    else:
        path_part, anchor = raw, ''
    if path_part.startswith('./'):
        path_part = path_part[2:]
    if not path_part:
        # #anchor-only
        return base, anchor
    target = (base.parent / path_part).resolve()
    return target, anchor


def main() -> int:
    md_files = [f for f in REPO.rglob('*.md') if not is_excluded(f)]
    html_files = [f for f in REPO.rglob('*.html') if not is_excluded(f)]

    broken: list[tuple[str, str, str]] = []  # (file, reason, link)

    for md in md_files:
        try:
            content = md.read_text(encoding='utf-8')
        except Exception:
            continue
        for m in MD_LINK.finditer(content):
            url = m.group('url').split()[0]
            # 외부 / 특수 제외
            if url.startswith(('http://', 'https://', 'mailto:', 'tel:', 'javascript:')):
                continue
            # 예시 placeholder (docs-renewal 자체 예시 `path`, `./path`)
            if url in ('path', './path'):
                continue
            if url.startswith('#'):
                # same-doc anchor
                if slugify(url[1:]) not in collect_headings(md):
                    broken.append((md.relative_to(REPO).as_posix(), 'anchor missing (same doc)', url))
                continue
            target, anchor = resolve_link(md, url)
            if not target.exists():
                broken.append((md.relative_to(REPO).as_posix(), 'file missing', url))
                continue
            if anchor and target.suffix == '.md':
                if slugify(anchor) not in collect_headings(target):
                    broken.append((md.relative_to(REPO).as_posix(), f'anchor missing in {target.name}', url))

    for html in html_files:
        try:
            content = html.read_text(encoding='utf-8')
        except Exception:
            continue
        for m in HTML_ATTR.finditer(content):
            url = m.group('url')
            if url.startswith(("'data:", "'data:image", "'")):
                continue  # Thymeleaf inline expression with data URL
            if url.startswith(('http://', 'https://', 'mailto:', 'tel:', 'javascript:', '/', 'data:', '@{', '${')):
                continue
            if url.startswith('#'):
                continue
            target, _ = resolve_link(html, url)
            if not target.exists():
                broken.append((html.relative_to(REPO).as_posix(), 'file missing', url))

    if broken:
        print(f"FAIL: {len(broken)} broken links")
        for f, reason, link in broken[:50]:
            print(f"  {f}: {reason} → {link}")
        if len(broken) > 50:
            print(f"  ... (+{len(broken) - 50} more)")
        return 1
    print("PASS: 0 broken links")
    return 0


if __name__ == "__main__":
    sys.exit(main())
