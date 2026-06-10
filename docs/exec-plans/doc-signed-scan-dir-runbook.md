# 운영 런북 — 날인본 스캔 저장소(DOC_SCAN_DIR) 정비 + 폴더 구조 전환

> 작성 2026-06-11 · 대상: **운영 서버(사내 내부망, Tomcat `C:\tomcat11`)** · 실행자: 인프라 담당(직접 수행)
> 관련 기능: `doc-signed-scan-upload` (사업문서 착수/기성/준공 최종 날인본 스캔 PDF)

---

## 0. 배경 (왜 하는가)

2026-06-11 점검 중 발견한 2가지:

1. **운영 Tomcat 에 `DOC_SCAN_DIR` 환경변수가 미설정** → 날인본이 의도한 `D:\swmanager-scan` 가 아니라
   기본 상대경로 `./uploads/scan`(= `C:\tomcat11\bin\uploads\scan`)에 저장되고 있었음.
   - 증거: doc_id 13 (기성계, 김포시 GeoNURIS) `signed_scan_path = C:\tomcat11\bin\uploads\scan\...`
   - ⚠ `C:\tomcat11\bin\uploads\scan` 은 WAR 재배포/톰캣 정리 시 유실 위험. **pg_dump 에도 미포함**.

2. **폴더 구조 변경 요구** — 기존 `{projId}_{사업명}/{문서종류}/{문서종류}_{사업명}_{yyyyMMdd}.pdf`
   → 신규 **`{문서종류}/{사업연도}/{시도}/{시군구}/{사업명}_{문서종류}.pdf`** (코드 반영 완료, 배포 필요).

이 런북은 ①새 구조 코드 배포 ②`DOC_SCAN_DIR` 설정 ③기존 파일 새 구조로 이전 + DB 보정 ④백업 루틴까지를 다룬다.

---

## 1. 사전 조건 / 체크리스트

- [ ] 위치: **사무실 내부망**(운영DB `INTERNAL_DB_HOST:5880/SW_Dept` 접속 가능). 집·출장지 불가.
- [ ] 운영DB 비밀번호(1Password) 확보 — psql 점검용.
- [ ] `D:\` 드라이브 존재 + 여유 용량 확인.
- [ ] 점검 시간대(업무 외) 확보 — 톰캣 재시작으로 잠깐 중단됨.
- [ ] 새 구조 반영된 **ROOT.war** 빌드 산출물(아래 2단계).

> 새 폴더 구조는 코드(`DocumentSignedScanService`)에 이미 반영됨(단위테스트 21개 통과).
> **배포 전까지 운영은 옛 구조로 업로드**하므로, 2단계(배포)와 4단계(이전)는 같은 점검창에서 연달아 수행한다.

---

## 2. 단계 A — 새 구조 코드(WAR) 배포

새 폴더 구조 로직이 운영에 떠야 신규 업로드가 새 트리에 저장된다.

```powershell
# 빌드 PC(또는 운영 PC)에서
Set-Location C:\Users\PUJ\eclipse-workspace\swmanager
git pull                              # 새 구조 커밋 반영분 받기
.\mvnw.cmd clean package -DskipTests=false   # 테스트 포함 빌드
# 산출물 target\*.war → 운영 배포 규칙상 파일명 ROOT.war 로
```

- 배포: 기존 운영 절차대로 `C:\tomcat11\webapps\ROOT.war` 교체(context root `/`).
- ⚠ **이름은 반드시 `ROOT.war`** (context root /).

> 배포 자체는 기존 WAR 운영 배포 가이드(`docs/exec-plans/archive/quotation-deploy.md`, OS 환경변수 4종/prod profile)를 따른다.
> 본 런북은 거기에 **DOC_SCAN_DIR 1종을 추가**하는 것.

---

## 3. 단계 B — `DOC_SCAN_DIR` 환경변수 설정 + 톰캣 재시작

`application.properties` 의 `file.scan-dir=${DOC_SCAN_DIR:./uploads/scan}` 가 D 드라이브를 보게 한다.

### 권장: Tomcat `setenv.bat` (catalina 가 항상 로드 → 서비스/콘솔 구동 무관하게 확실)

`C:\tomcat11\bin\setenv.bat` 에 한 줄 추가(파일 없으면 생성):

```bat
set "DOC_SCAN_DIR=D:\swmanager-scan"
```

### 대안: 머신 환경변수 (서비스로 구동 시)

```powershell
[Environment]::SetEnvironmentVariable('DOC_SCAN_DIR','D:\swmanager-scan','Machine')
# 서비스가 머신 env 를 읽으려면 재시작 필요(아래)
```

### 폴더 준비 + 재시작

```powershell
if(-not (Test-Path 'D:\swmanager-scan')){ New-Item -ItemType Directory 'D:\swmanager-scan' | Out-Null }
# 톰캣 재시작 (서비스명은 환경에 맞게)
Restart-Service -Name 'Tomcat11' -ErrorAction SilentlyContinue
# 콘솔 구동이면: C:\tomcat11\bin\shutdown.bat ; C:\tomcat11\bin\startup.bat
```

### 확인

재시작 후 임의 사업문서 1건에 날인본을 시험 업로드 → `D:\swmanager-scan\...` 에 떨어지는지 확인.
(또는 DB 의 신규 `signed_scan_path` 가 `D:\swmanager-scan` 로 시작하는지 확인.)

---

## 4. 단계 C — 기존 파일 새 구조로 이전 + DB 경로 보정

기존 `C:\tomcat11\bin\uploads\scan` 에 옛 구조로 쌓인 파일을, 새 구조 `D:\swmanager-scan` 로 옮기고 DB `signed_scan_path` 를 보정한다.

> **현재 대상은 doc_id 13 한 건뿐**(2026-06-11 기준). 아래 스크립트는 향후 다건도 처리하도록 일반화돼 있다.
> 새 경로 규칙(코드와 동일): `{문서종류}/{사업연도}/{시도}/{시군구}/{사업명}_{문서종류}.pdf`
> - 문서종류: COMMENCE→착수계 / INTERIM→기성계 / COMPLETION→준공계
> - null/빈값 폴더: 연도→`연도미상`, 시도→`시도미상`, 시군구→`시군구미상`, 사업명→`사업미상`
> - 정제: `\ / : * ? " < > |` 및 제어문자 제거, 공백 1칸 압축, 80자 컷

### C-1. 먼저 DRY-RUN (이동 없이 계획만 출력)

```powershell
$env:PGPASSWORD = '<운영DB 비번 — 1Password>'
$env:PGCLIENTENCODING = 'UTF8'
$psql = 'C:\Users\PUJ\PostgreSQL\16\bin\psql.exe'
$H='<INTERNAL_DB_HOST>'; $P='5880'; $U='SW_Dept'; $DB='SW_Dept'
$BASE = 'D:\swmanager-scan'

function Convert-Label([string]$t){ switch($t){ 'COMMENCE'{'착수계'} 'INTERIM'{'기성계'} 'COMPLETION'{'준공계'} default{'기타'} } }
function Convert-Seg([string]$raw,[string]$fallback){
  if([string]::IsNullOrWhiteSpace($raw)){ return $fallback }
  $s = ($raw -replace '[\\/:*?"<>|]','') -replace '[\x00-\x1F]','' -replace '\s+',' '
  $s = $s.Trim(); if($s.Length -gt 80){ $s = $s.Substring(0,80).Trim() }
  if([string]::IsNullOrEmpty($s)){ $fallback } else { $s }
}

# 대상 행 조회 (탭 구분)
$rows = & $psql -h $H -p $P -U $U -d $DB -A -F"`t" -t -c @"
SELECT d.doc_id, d.doc_type, p.year, p.city_nm, p.dist_nm, p.proj_nm, d.signed_scan_path
FROM tb_document d JOIN sw_pjt p ON p.proj_id=d.proj_id
WHERE d.signed_scan_path IS NOT NULL
ORDER BY d.doc_id;
"@

$plan = foreach($line in $rows){
  if([string]::IsNullOrWhiteSpace($line)){ continue }
  $c = $line -split "`t"
  $docId=$c[0]; $label=Convert-Label $c[1]
  $year = if([string]::IsNullOrWhiteSpace($c[2])){'연도미상'}else{$c[2].Trim()}
  $city=Convert-Seg $c[3] '시도미상'; $dist=Convert-Seg $c[4] '시군구미상'
  $proj=Convert-Seg $c[5] '사업미상'; $old=$c[6]
  $new = Join-Path $BASE (Join-Path $label (Join-Path $year (Join-Path $city (Join-Path $dist "${proj}_${label}.pdf"))))
  [PSCustomObject]@{ docId=$docId; old=$old; new=$new; oldExists=(Test-Path $old) }
}
$plan | Format-List

# 충돌 검사: 서로 다른 행이 같은 new 경로로 매핑되면(같은 문서종류/연도/시도/시군구/사업명) 중단
$dups = $plan | Group-Object new | Where-Object Count -gt 1
if($dups){
  $dups | ForEach-Object { Write-Warning "new 경로 충돌: $($_.Name)  <- doc $($_.Group.docId -join ',')" }
  throw "new 경로 충돌 발견 — 이전 중단. 사업명/지역 정제 결과 수동 확인 필요."
}
```

→ 출력된 `new` 경로와 `oldExists=True` 를 **육안 검수**한다. (doc 13 예상 new:
`D:\swmanager-scan\기성계\2026\경기도\김포시\2026년 부동산종합공부시스템 GeoNURIS for KRAS v1.0 유지보수 용역_기성계.pdf`)

### C-2. 무손실 이전 — Copy → DB 보정 (원본은 5단계 검증 후에만 삭제)

> ⛔ **Move 금지.** 파일을 먼저 **복사**하고 DB 를 보정한 뒤, 5단계 검증을 통과한 다음에만 원본을 지운다.
> 어느 단계가 실패해도 원본(`C:\tomcat11\bin\uploads\scan`)이 그대로 남아 무손실이다.

```powershell
$copied = @()
foreach($r in $plan){
  if(-not $r.oldExists){ Write-Warning "원본 없음, 건너뜀: doc $($r.docId) $($r.old)"; continue }
  if(Test-Path -LiteralPath $r.new){ Write-Error "대상 이미 존재 — 충돌, 건너뜀: $($r.new)"; continue }  # 덮어쓰기 방지
  $dir = Split-Path $r.new -Parent
  New-Item -ItemType Directory -Path $dir -Force | Out-Null
  try {
    Copy-Item -LiteralPath $r.old -Destination $r.new -ErrorAction Stop   # -Force 미사용(기존 대상 보호)
  } catch { Write-Error "복사 실패 doc $($r.docId): $_"; continue }
  # 복사 성공 → DB path 보정 (경로의 작은따옴표 이스케이프)
  $newEsc = $r.new -replace "'","''"
  & $psql -h $H -p $P -U $U -d $DB -v ON_ERROR_STOP=1 -c "UPDATE tb_document SET signed_scan_path='$newEsc' WHERE doc_id=$($r.docId);"
  if($LASTEXITCODE -ne 0){
    Write-Error "DB 보정 실패 doc $($r.docId) — 방금 복사한 새 파일 제거(원본 유지)"
    Remove-Item -LiteralPath $r.new -Force -ErrorAction SilentlyContinue   # 새 파일만 롤백, 원본은 그대로
    continue
  }
  $copied += $r
  Write-Host "OK doc $($r.docId) -> $($r.new)"
}
```

→ 여기서 멈추고 **5단계 검증을 먼저 수행**한다. 검증 통과 후 아래 C-3 으로 원본을 정리한다.

### C-3. (5단계 검증 통과 후에만) 원본 삭제 + 빈 폴더 정리

```powershell
# 위 $copied(복사+DB보정 성공분)만 원본 삭제
foreach($r in $copied){ Remove-Item -LiteralPath $r.old -Force -ErrorAction SilentlyContinue }
# 옛 구조 빈 폴더 정리(파일 없을 때만)
Get-ChildItem 'C:\tomcat11\bin\uploads\scan' -Recurse -Directory -ErrorAction SilentlyContinue |
  Sort-Object FullName -Descending |
  Where-Object { -not (Get-ChildItem $_.FullName -File -Recurse) } |
  Remove-Item -Force -ErrorAction SilentlyContinue
```

---

## 5. 단계 D — 검증

```powershell
# (1) DB: 모든 날인본 경로가 D:\swmanager-scan 로 시작하는가
& $psql -h $H -p $P -U $U -d $DB -A -c "SELECT doc_id, signed_scan_path FROM tb_document WHERE signed_scan_path IS NOT NULL AND signed_scan_path NOT LIKE 'D:\swmanager-scan%';"
#   → 0행이어야 함

# (2) 파일 실제 존재 + 새 구조 확인
& $psql -h $H -p $P -U $U -d $DB -A -t -c "SELECT signed_scan_path FROM tb_document WHERE signed_scan_path IS NOT NULL;" |
  ForEach-Object { if($_){ "{0}  exists={1}" -f $_, (Test-Path $_) } }
#   → 전부 exists=True

# (3) 화면: 사업문서 목록에서 해당 문서 '날인본 다운로드' 동작 확인
```

---

## 6. 단계 E — 백업 루틴 (필수, pg_dump 미포함)

`D:\swmanager-scan` 는 DB 백업에 안 들어가므로 **별도 백업**이 반드시 필요하다.

예) 일 1회 NAS/별도 디스크로 미러 (작업 스케줄러 등록):

```powershell
# backup-scan.ps1 — 매일 새벽 robocopy 미러
$src='D:\swmanager-scan'; $dst='\\NAS\backup\swmanager-scan'   # 실제 백업 대상으로 교체
robocopy $src $dst /MIR /R:2 /W:5 /NP /LOG+:D:\swmanager-scan-backup.log
```

```powershell
# 스케줄 등록(예: 매일 02:30)
$act = New-ScheduledTaskAction -Execute 'powershell.exe' -Argument '-NoProfile -File D:\ops\backup-scan.ps1'
$trg = New-ScheduledTaskTrigger -Daily -At 2:30AM
Register-ScheduledTask -TaskName 'swmanager-scan-backup' -Action $act -Trigger $trg -RunLevel Highest
```

---

## 7. 롤백

- **코드(WAR)**: 직전 ROOT.war 백업본으로 교체 후 톰캣 재시작. (새 구조는 경로 계산만 바꿔 데이터 파괴 없음)
- **DOC_SCAN_DIR**: `setenv.bat` 줄 제거 또는 머신 env 삭제 후 재시작 → 다시 `./uploads/scan` 로.
- **파일 이전(4단계)**: C-2 가 **Copy 기본**(원본 보존)이라, 복사/DB보정/검증 중 어디서 실패해도 원본(`C:\tomcat11\bin\uploads\scan`)이 그대로 남아 무손실.
  - 실패 시: 방금 복사된 새 파일만 제거(C-2 가 DB 보정 실패 시 자동 롤백). 원본은 두고 원인 해결 후 재시도.
  - 원본 삭제는 5단계 검증 통과 후 **C-3 에서만** 수행 → 검증 전이면 언제든 안전.

---

## 부록 — 한 줄 요약 (doc 13 단건만 빠르게)

현재 실제 대상이 1건이므로 수동으로도 가능:

```powershell
$old='C:\tomcat11\bin\uploads\scan\550_2026년 부동산종합공부시스템 GeoNURIS for KRAS v1.0 유지보수 용역\기성계\기성계_2026년 부동산종합공부시스템 GeoNURIS for KRAS v1.0 유지보수 용역_20260610.pdf'
$new='D:\swmanager-scan\기성계\2026\경기도\김포시\2026년 부동산종합공부시스템 GeoNURIS for KRAS v1.0 유지보수 용역_기성계.pdf'
New-Item -ItemType Directory (Split-Path $new -Parent) -Force | Out-Null
Copy-Item -LiteralPath $old -Destination $new   # 검증 후 원본 삭제
# DB: UPDATE tb_document SET signed_scan_path='D:\swmanager-scan\기성계\2026\경기도\김포시\2026년 부동산종합공부시스템 GeoNURIS for KRAS v1.0 유지보수 용역_기성계.pdf' WHERE doc_id=13;
```
