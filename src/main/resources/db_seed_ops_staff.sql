-- [ops-fault-support staff] tb_staff 직원 디렉터리 시드 (멱등 INSERT).
-- SW지원팀 우선. 전 직원(정도유아이티 조직도 41이미지)은 전사 후 본 파일에 확장.
INSERT INTO tb_staff (name, position, org_unit_id, active, sort_order)
SELECT '박욱진', '차장', (SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'), TRUE, 10
 WHERE NOT EXISTS (SELECT 1 FROM tb_staff WHERE name='박욱진'
   AND org_unit_id=(SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'));
INSERT INTO tb_staff (name, position, org_unit_id, active, sort_order)
SELECT '김한준', '대리', (SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'), TRUE, 20
 WHERE NOT EXISTS (SELECT 1 FROM tb_staff WHERE name='김한준'
   AND org_unit_id=(SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'));
INSERT INTO tb_staff (name, position, org_unit_id, active, sort_order)
SELECT '서현규', '사원', (SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'), TRUE, 30
 WHERE NOT EXISTS (SELECT 1 FROM tb_staff WHERE name='서현규'
   AND org_unit_id=(SELECT unit_id FROM tb_org_unit WHERE name='SW지원팀' AND unit_type='TEAM'));
