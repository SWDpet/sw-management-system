import json

# Load hwpx parsed data
with open('C:/Users/PUJ/eclipse-workspace/swmanager/hwpx_results.json', 'r', encoding='utf-8') as f:
    hwpx = json.load(f)

# Load DB infra data
with open('C:/Users/PUJ/eclipse-workspace/swmanager/infra_data.json', 'r', encoding='utf-8') as f:
    db_data = json.load(f)

# Build a lookup: distNm -> {serverType -> record}
db_lookup = {}
for r in db_data:
    dist = r['distNm']
    stype = r['serverType']
    if dist not in db_lookup:
        db_lookup[dist] = {}
    # Map WEB -> AP
    key = 'AP' if stype == 'WEB' else stype
    db_lookup[dist][key] = r

# Match hwpx data to DB records
print("=" * 100)
print(f"{'시군구':<8} {'타입':<4} {'server_id':<10} {'기존CPU':<25} {'신규CPU':<30}")
print("=" * 100)

matched = []
unmatched = []

for dist, data in sorted(hwpx.items()):
    if 'error' in data:
        unmatched.append(f"{dist}: {data['error']}")
        continue
    
    for stype, specs in data.items():
        if not specs:  # empty dict
            unmatched.append(f"{dist} {stype}: 파싱된 데이터 없음")
            continue
            
        if dist in db_lookup and stype in db_lookup[dist]:
            db_rec = db_lookup[dist][stype]
            existing_cpu = db_rec.get('cpuSpec', '')
            new_cpu = specs.get('cpu', '')
            
            print(f"{dist:<8} {stype:<4} {db_rec['serverId']:<10} {existing_cpu:<25} {new_cpu:<30}")
            matched.append({
                'dist': dist,
                'type': stype,
                'serverId': db_rec['serverId'],
                'existing': {
                    'cpu': db_rec.get('cpuSpec', ''),
                    'memory': db_rec.get('memorySpec', ''),
                    'disk': db_rec.get('diskSpec', ''),
                    'network': db_rec.get('networkSpec', ''),
                    'power': db_rec.get('powerSpec', ''),
                    'os_detail': db_rec.get('osDetail', ''),
                    'model': db_rec.get('serverModel', ''),
                },
                'new': specs
            })
        else:
            unmatched.append(f"{dist} {stype}: DB에 매칭되는 레코드 없음 (DB에 {dist} 있음: {dist in db_lookup})")

print()
print("=" * 100)
print(f"매칭 성공: {len(matched)}건")
print(f"매칭 실패: {len(unmatched)}건")
print()

if unmatched:
    print("--- 매칭 실패 목록 ---")
    for u in unmatched:
        print(f"  {u}")

print()
print("=" * 100)
print("임포트 상세 내역 (변경될 데이터)")
print("=" * 100)

for m in matched:
    print(f"\n[{m['dist']} - {m['type']}서버] server_id={m['serverId']}")
    fields = ['model', 'cpu', 'memory', 'disk', 'network', 'power', 'os']
    db_fields = ['model', 'cpu', 'memory', 'disk', 'network', 'power', 'os_detail']
    col_names = ['server_model', 'cpu_spec', 'memory_spec', 'disk_spec', 'network_spec', 'power_spec', 'os_detail']
    
    for field, db_field, col in zip(fields, db_fields, col_names):
        old_val = m['existing'].get(db_field, '') or ''
        new_val = m['new'].get(field, '') or ''
        if new_val:
            marker = " [NEW]" if not old_val else (" [변경]" if old_val != new_val else " [동일]")
            print(f"  {col:<15}: {old_val:<35} -> {new_val}{marker}")

# Save matched data for import
with open('C:/Users/PUJ/eclipse-workspace/swmanager/import_plan.json', 'w', encoding='utf-8') as f:
    json.dump(matched, f, ensure_ascii=False, indent=2)

