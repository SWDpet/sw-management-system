"""Convert Refero JSON API response into a DESIGN.md-style markdown file.

Usage: python convert.py <uuid> <out_path>
"""
import json
import sys
import urllib.request
from pathlib import Path

API = 'https://styles.refero.design/api/styles/{uuid}'
PAGE = 'https://styles.refero.design/style/{uuid}'


def fetch_json(uuid: str) -> dict:
    with urllib.request.urlopen(API.format(uuid=uuid)) as r:
        return json.loads(r.read().decode('utf-8'))


def slugify(name: str) -> str:
    s = (name or '').lower().strip()
    out = []
    for ch in s:
        if ch.isalnum():
            out.append(ch)
        elif ch in ' -_':
            out.append('-')
    slug = ''.join(out)
    while '--' in slug:
        slug = slug.replace('--', '-')
    return slug.strip('-') or 'untitled'


def cell(v) -> str:
    if v is None:
        return ''
    if isinstance(v, list):
        v = ', '.join(str(x) for x in v)
    s = str(v).replace('|', '\\|').replace('\n', ' ').strip()
    return s


def kv_table(rows: list[tuple[str, str]]) -> str:
    lines = ['| Key | Value |', '| --- | --- |']
    for k, v in rows:
        lines.append(f'| {cell(k)} | {cell(v)} |')
    return '\n'.join(lines)


def render(data: dict) -> str:
    style = data['style']
    site = style.get('siteName', 'Unknown')
    src_url = style.get('url', '')
    page_url = PAGE.format(uuid=style['id'])
    ds = style.get('fullResult', {}).get('designSystem', {}) or {}
    raw = style.get('fullResult', {}).get('raw', {}) or {}

    out: list[str] = []
    out.append(f'# {site} — Design Reference')
    out.append('')
    if ds.get('northStar'):
        out.append(f'> {ds["northStar"]}')
        out.append('')

    meta_rows = [
        ('Source site', f'[{src_url}]({src_url})' if src_url else '-'),
        ('Refero page', f'[{page_url}]({page_url})'),
        ('Theme', ds.get('theme') or style.get('colorScheme', '-')),
        ('Industry', ds.get('industry') or style.get('industry', '-')),
    ]
    out.append('## Meta')
    out.append('')
    out.append(kv_table(meta_rows))
    out.append('')

    if ds.get('description'):
        out.append('## Overview')
        out.append('')
        out.append(ds['description'])
        out.append('')

    # Colors
    colors = ds.get('colors') or []
    if colors:
        out.append('## Colors')
        out.append('')
        out.append('| Name | Hex | Group | Role |')
        out.append('| --- | --- | --- | --- |')
        for c in colors:
            out.append(f"| {cell(c.get('name'))} | `{cell(c.get('hex'))}` | {cell(c.get('group'))} | {cell(c.get('role'))} |")
        out.append('')

    # Typography
    typ = ds.get('typography') or []
    if typ:
        out.append('## Typography')
        out.append('')
        for t in typ:
            fam = t.get('family') or t.get('name', '')
            out.append(f'### {fam}')
            out.append('')
            tt_rows = []
            for k in ('weight', 'weights', 'sizes', 'lineHeight', 'letterSpacing', 'fontFeatureSettings', 'substitute', 'role'):
                if t.get(k):
                    tt_rows.append((k, t[k]))
            if tt_rows:
                out.append(kv_table(tt_rows))
                out.append('')

    type_scale = ds.get('typeScale') or []
    if type_scale:
        out.append('### Type scale')
        out.append('')
        out.append('| Role | Size | Weight | Line height | Letter spacing |')
        out.append('| --- | --- | --- | --- | --- |')
        for s in type_scale:
            out.append(f"| {cell(s.get('role') or s.get('name'))} | {cell(s.get('size') or s.get('fontSize'))} | {cell(s.get('weight') or s.get('fontWeight'))} | {cell(s.get('lineHeight'))} | {cell(s.get('letterSpacing'))} |")
        out.append('')

    # Spacing
    spacing = ds.get('spacing') or {}
    if spacing:
        out.append('## Spacing')
        out.append('')
        for group, val in spacing.items():
            if isinstance(val, dict):
                out.append(f'### {group}')
                out.append('')
                out.append(kv_table([(k, v) for k, v in val.items()]))
                out.append('')
            else:
                out.append(f'- **{group}** — {cell(val)}')
        if not all(isinstance(v, dict) for v in spacing.values()):
            out.append('')

    # Surfaces
    surfaces = ds.get('surfaces') or []
    if surfaces:
        out.append('## Surfaces')
        out.append('')
        out.append('| Name | Hex | Level | Purpose |')
        out.append('| --- | --- | --- | --- |')
        for s in surfaces:
            out.append(f"| {cell(s.get('name'))} | `{cell(s.get('hex'))}` | {cell(s.get('level'))} | {cell(s.get('purpose') or s.get('description'))} |")
        out.append('')

    # Components
    comps = ds.get('components') or []
    if comps:
        out.append('## Components')
        out.append('')
        for c in comps:
            out.append(f"### {c.get('name', '(unnamed)')}")
            out.append('')
            if c.get('role'):
                out.append(f"**Role:** {c['role']}")
                out.append('')
            if c.get('description'):
                out.append(c['description'])
                out.append('')
            for key in ('anatomy', 'states', 'variants', 'tokens', 'examples', 'usage'):
                v = c.get(key)
                if not v:
                    continue
                if isinstance(v, str):
                    out.append(f'**{key.capitalize()}:** {v}')
                    out.append('')
                elif isinstance(v, list):
                    out.append(f'**{key.capitalize()}:**')
                    for item in v:
                        if isinstance(item, str):
                            out.append(f'- {item}')
                        else:
                            out.append(f'- {json.dumps(item, ensure_ascii=False)}')
                    out.append('')
                elif isinstance(v, dict):
                    out.append(f'**{key.capitalize()}:**')
                    for k2, v2 in v.items():
                        out.append(f'- **{k2}**: {v2}')
                    out.append('')

    # Layout / Imagery / Elevation
    if ds.get('layout'):
        out.append('## Layout')
        out.append('')
        out.append(ds['layout'])
        out.append('')
    if ds.get('imagery'):
        out.append('## Imagery')
        out.append('')
        out.append(ds['imagery'])
        out.append('')
    if ds.get('elevationPhilosophy'):
        out.append('## Elevation philosophy')
        out.append('')
        out.append(ds['elevationPhilosophy'])
        out.append('')

    # Dos / Donts
    dos = ds.get('dos') or []
    donts = ds.get('donts') or []
    if dos or donts:
        out.append('## Dos & Donts')
        out.append('')
        if dos:
            out.append('### Do')
            out.append('')
            for d in dos:
                out.append(f'- {d}')
            out.append('')
        if donts:
            out.append("### Don't")
            out.append('')
            for d in donts:
                out.append(f'- {d}')
            out.append('')

    # Custom sections
    cs = ds.get('customSections') or []
    if cs:
        out.append('## Notes')
        out.append('')
        for s in cs:
            out.append(f"### {s.get('title', '')}")
            out.append('')
            out.append(s.get('content', '') or s.get('body', '') or s.get('description', ''))
            out.append('')

    out.append('---')
    out.append(f'_Source: {page_url}_')
    out.append('')
    return '\n'.join(out)


def main():
    uuid = sys.argv[1]
    out_path = Path(sys.argv[2])
    data = fetch_json(uuid)
    md = render(data)
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out_path.write_text(md, encoding='utf-8')
    print(f'wrote {out_path} ({len(md)} chars)')


if __name__ == '__main__':
    main()
