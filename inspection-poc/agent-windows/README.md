# UPIS 점검 자동화 — Windows AP 에이전트 (PoC)

점검 대상 서버(Windows Server 2012R2 이상)에서 실행되어 점검 항목을 자동 수집하고, 모니터에 QR 코드로 결과를 표시하는 모듈.

## 디렉토리
```
agent-windows/
├── inspect.ps1                  진입점
├── config/site.dyg.json         사이트별 설정 (임계값, 경로 등)
├── lib/
│   ├── Common.ps1               JSON/로그/임계값 유틸
│   ├── Collector.ps1            checks/ 디스패치 + 스냅샷 빌드
│   ├── Snapshot.ps1             스냅샷 저장/직전 회차 조회
│   ├── Differ.ps1               델타 계산
│   └── QrPayload.ps1            압축+base45+CRC+SHA-1+프레임 분할
├── checks/                      체크 모듈 20개 (AP 12 + GIS 6 + LED 4묶음 = 24 항목)
├── snapshots/                   회차 결과 영구 보관
└── out/                         최신 회차 산출물
    ├── delta.json
    ├── frames.json              QR 변환용 프레임 시퀀스
    └── summary.html             모니터 표시용 요약
```

## 점검 항목 (24개)

### AP 서버 — 14개
| ID | 카테고리 | 자동/수동 |
|---|---|---|
| ap.hw.cpu / memory / adapter | 구성 | A |
| ap.perf.cpu_pct / mem_pct | 성능 | A |
| ap.disk.c / d (외 마운트 자동) | 파일시스템 | A |
| ap.log.system_err / security_err | 로그 | A |
| ap.net.routes / ip | 네트워크 | A |
| ap.security.users | 보안 | A |
| ap.led.system / psu / disk / ap.cable | 육안 | M (수동 입력 대기) |

### GIS — 6개
| ID | 자동/수동 |
|---|---|
| gis.gss.running | A |
| gis.gss.log_purge | A (기본 dry-run) |
| gis.gws.running | A |
| gis.gws.log_purge | A |
| gis.gws.store_size | A |
| gis.gws.http | A |

## 실행
```powershell
# 사이트 설정 지정
.\inspect.ps1 -ConfigPath .\config\site.dyg.json

# 특정 체크만 실행 (디버깅)
.\inspect.ps1 -OnlyChecks ap-perf-cpu,gis-store-size
```

## 라운드트립 검증
에이전트 산출물(`out/frames.json`)을 PoC 디코더로 검증 가능:
```powershell
cd ..\decoder
node decode.js ..\agent-windows\out\frames.json
```

## PS 5.1 호환성 주의사항
다음 4가지 함정에 걸려서 디버깅함:
1. **UTF-8 BOM 필요** — .ps1 파일이 UTF-8 BOM 없이 저장되면 한글이 깨짐
2. **strict mode** — 단일 객체 `.Count` 호출 에러. strict mode 미사용
3. **byte -shl 결과 truncation** — `[byte]65 -shl 8 = 0`. int 캐스트 필수
4. **32비트 hex 리터럴의 부호확장** — `[int64]0xEDB88320`이 음수 되는 문제. 양수 십진수로 명시

## 운영 시나리오
1. 점검자가 콘솔에 로그인 후 `.\inspect.ps1` 실행 (약 15초 소요)
2. `out\summary.html`이 자동 표시 (또는 점검자가 직접 열기)
3. 화면에 QR 2장 표출 → 갤럭시탭으로 스캔
4. 사외망 복귀 후 SW 사업관리시스템에 업로드

## 다음 단계
- [ ] AIX DB 에이전트 (ksh, 동일 컨트랙트)
- [ ] QR 렌더링 자체화 (현재 frames.json만 생성. QR 이미지는 별도 단계)
- [ ] 갤럭시탭 PWA (BarcodeDetector + decode.js 포팅)
- [ ] SW Manager `/api/inspection/qr-batch` 엔드포인트
