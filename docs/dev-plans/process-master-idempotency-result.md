# 재시작 #1 체크

- 실행: Mon Apr 20 16:10:40 KST 2026

- (a) tb_process_master COUNT=5 ✅
- (a) tb_service_purpose COUNT=5 ✅
- (b) tb_process_master DISTINCT=5 ✅
- (b) tb_service_purpose DISTINCT=5 ✅
- (c) tb_process_master dup groups=0 ✅
- (c) tb_service_purpose dup groups=0 ✅
- (d) UNIQUE constraint(process)=1 ✅
- (d) UNIQUE index(purpose)=1 ✅
- (e) 수동 INSERT process r1=0 ✅
- (e) 수동 INSERT purpose r2=0 ✅

## 결과

✅ PASS

---

# 재시작 #2 체크

- 실행: Mon Apr 20 16:10:55 KST 2026

- (a) tb_process_master COUNT=5 ✅
- (a) tb_service_purpose COUNT=5 ✅
- (b) tb_process_master DISTINCT=5 ✅
- (b) tb_service_purpose DISTINCT=5 ✅
- (c) tb_process_master dup groups=0 ✅
- (c) tb_service_purpose dup groups=0 ✅
- (d) UNIQUE constraint(process)=1 ✅
- (d) UNIQUE index(purpose)=1 ✅
- (e) 수동 INSERT process r1=0 ✅
- (e) 수동 INSERT purpose r2=0 ✅

## 결과

✅ PASS

---

# 재시작 #3 체크

- 실행: Mon Apr 20 16:11:09 KST 2026

- (a) tb_process_master COUNT=5 ✅
- (a) tb_service_purpose COUNT=5 ✅
- (b) tb_process_master DISTINCT=5 ✅
- (b) tb_service_purpose DISTINCT=5 ✅
- (c) tb_process_master dup groups=0 ✅
- (c) tb_service_purpose dup groups=0 ✅
- (d) UNIQUE constraint(process)=1 ✅
- (d) UNIQUE index(purpose)=1 ✅
- (e) 수동 INSERT process r1=0 ✅
- (e) 수동 INSERT purpose r2=0 ✅

## 결과

✅ PASS

---

