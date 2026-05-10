"""Fetch every Refero style listed on the homepage and write one .md per style + INDEX.md."""
import json
import re
import sys
import time
import urllib.request
import urllib.error
from pathlib import Path

try:
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
except Exception:
    pass

sys.path.insert(0, str(Path(__file__).parent))
from convert import fetch_json, render, slugify, PAGE  # type: ignore

ROOT = Path(r'C:/Users/ukjin/sw-management-system/docs/design-references')
ROOT.mkdir(parents=True, exist_ok=True)

HOME = 'https://styles.refero.design/'


UUID_RE = re.compile(r'/style/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})')
MAX_TOTAL = 500  # safety cap
CACHE_DIR = ROOT / '_tmp' / 'json_cache'
CACHE_DIR.mkdir(parents=True, exist_ok=True)


def fetch_json_cached(uuid: str) -> dict:
    p = CACHE_DIR / f'{uuid}.json'
    if p.exists():
        return json.loads(p.read_text(encoding='utf-8'))
    data = fetch_json(uuid)
    p.write_text(json.dumps(data, ensure_ascii=False), encoding='utf-8')
    return data


def collect_seed_uuids() -> list[str]:
    """Pull every /style/{uuid} link from the homepage HTML as the BFS seed set."""
    with urllib.request.urlopen(HOME) as r:
        html = r.read().decode('utf-8', errors='replace')
    seen = []
    for u in UUID_RE.findall(html):
        if u not in seen:
            seen.append(u)
    return seen


def neighbors(data: dict) -> list[str]:
    out = []
    for s in data.get('similar', []) or []:
        sid = s.get('id')
        if sid:
            out.append(sid)
    # Also dive into the designSystem.similar array (different shape)
    ds = data.get('style', {}).get('fullResult', {}).get('designSystem', {}) or {}
    for s in ds.get('similar', []) or []:
        if isinstance(s, dict):
            sid = s.get('id') or s.get('uuid')
            if sid:
                out.append(sid)
    return out


def main():
    seed = collect_seed_uuids()
    print(f'seed: {len(seed)} UUIDs from homepage')

    queue = list(seed)
    visited: set[str] = set()
    index_rows: list[tuple[str, str, str, str, str]] = []
    fail = 0

    while queue and len(visited) < MAX_TOTAL:
        uuid = queue.pop(0)
        if uuid in visited:
            continue
        visited.add(uuid)

        try:
            data = fetch_json_cached(uuid)
        except urllib.error.HTTPError as e:
            print(f'  [{len(visited):3d}/?] {uuid} -> HTTP {e.code}, skipping')
            fail += 1
            continue
        except Exception as e:
            print(f'  [{len(visited):3d}/?] {uuid} -> error {e}, skipping')
            fail += 1
            continue

        # enqueue neighbors
        new_neighbors = 0
        for n in neighbors(data):
            if n not in visited and n not in queue:
                queue.append(n)
                new_neighbors += 1

        style = data['style']
        brand = style.get('siteName', f'style-{uuid[:8]}')
        slug = slugify(brand)
        out_path = ROOT / f'{slug}.md'
        if out_path.exists():
            existing = out_path.read_text(encoding='utf-8', errors='ignore')
            if uuid not in existing:
                out_path = ROOT / f'{slug}-{uuid[:8]}.md'

        md = render(data)
        out_path.write_text(md, encoding='utf-8')

        ds = style.get('fullResult', {}).get('designSystem', {}) or {}
        index_rows.append((
            brand,
            ds.get('theme') or style.get('colorScheme', '-'),
            ds.get('industry') or style.get('industry', '-'),
            (ds.get('northStar') or '').replace('|', '\\|').replace('\n', ' '),
            f'[{out_path.name}]({out_path.name})',
        ))
        print(f'  [{len(visited):3d}] {brand:30s} -> {out_path.name} (+{new_neighbors} new in queue, queue={len(queue)})')
        time.sleep(0.2)

    print(f'\ntotal visited: {len(visited)}, written: {len(index_rows)}, failures: {fail}, queue remaining: {len(queue)}')

    # Build INDEX.md
    lines = [
        '# Design References — INDEX',
        '',
        f'_Sourced from <{HOME}> · {len(index_rows)} styles_',
        '',
        '| Brand | Theme | Industry | North star | File |',
        '| --- | --- | --- | --- | --- |',
    ]
    for brand, theme, industry, north, link in sorted(index_rows, key=lambda r: r[0].lower()):
        lines.append(f'| {brand} | {theme} | {industry} | {north} | {link} |')
    lines.append('')
    lines.append('---')
    lines.append('')
    lines.append('각 파일은 Refero 의 `/api/styles/{uuid}` 응답을 기반으로 자동 변환된 markdown 입니다.')
    lines.append('재생성 스크립트: `_tmp/fetch_all.py` + `_tmp/convert.py`')
    lines.append('')
    (ROOT / 'INDEX.md').write_text('\n'.join(lines), encoding='utf-8')
    print(f'wrote INDEX.md with {len(index_rows)} entries')


if __name__ == '__main__':
    main()
