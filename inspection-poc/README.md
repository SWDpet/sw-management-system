# 점검 자동화 QR 반출 PoC

폐쇄망 서버에서 생성한 점검 결과(JSON)를 화면에 QR로 표시 → 갤럭시탭 PWA로 광학 스캔 → 사외망 복귀 후 SW Manager로 업로드하는 파이프라인의 핵심부(인코딩/디코딩) 검증용 PoC.

## 디렉토리
```
inspection-poc/
├── sample_payload.json       단양군 UPIS 1회차 모사 페이로드 (1.5KB)
├── stress_payload.json       5사이트×4티어 스트레스 페이로드 (31KB)
├── stress_payload.py         스트레스 페이로드 생성기
├── encoder/
│   └── encode.py             서버 측 인코더 (gzip + base45 + QR PNG)
├── decoder/
│   ├── decode.js             Node 디코더 레퍼런스 (CRC + 조립 + SHA-1 + gunzip)
│   └── fault_test.js         결함 주입 테스트 (CRC/누락/SHA 변조)
├── pwa/
│   ├── scanner.html          PWA 스캐너 페이지 (카메라 + BarcodeDetector/jsQR)
│   ├── decoder.mjs           브라우저용 디코더 (DecompressionStream + crypto.subtle)
│   └── _smoke.mjs            decoder.mjs 라운드트립 + 결함 거부 회귀 테스트
├── viewer.html               갤럭시탭으로 스캔하기 위한 QR 표시 페이지
├── out/                      sample_payload 인코딩 결과
├── out_stress/               stress_payload 인코딩 결과 (단일 청크)
├── out_multichunk/           stress_payload + max-bytes=400 (4 청크)
└── out_fault/                결함 주입 테스트 임시 파일
```

## 동작 확인 결과

| 시나리오 | raw / gzip / base45 | QR 프레임 | 결과 |
|---|---|---|---|
| sample (단양군 1회차) | 1597 / 681 / 1022 | 2 (헤더+데이터 1) | ✓ |
| stress (5사이트) | 31920 / 860 / 1290 | 2 (압축률 97%) | ✓ |
| stress (400자 청크 강제) | 31920 / 860 / 1290 | 5 (헤더+데이터 4) | ✓ |
| CRC 변조 | — | — | ✓ 거부 |
| 청크 누락 | — | — | ✓ 거부 |
| SHA-1 변조 | — | — | ✓ 거부 |

## 사용법
```powershell
# 1. 의존성
pip install qrcode[pil] base45

# 2. PoC 실행 (sample)
cd inspection-poc/encoder
python encode.py --in ../sample_payload.json --out-dir ../out

# 3. 디코더로 라운드트립 검증
cd ../decoder
node decode.js ../out/frames.json

# 4. 결함 주입 테스트
node fault_test.js

# 5. 브라우저 디코더 회귀 (Node 24+)
cd ../pwa
node _smoke.mjs              # 3건 라운드트립 + 3건 결함 거부

# 6. 갤럭시탭 검증
#    - inspection-poc/ 디렉토리에 간단한 HTTP 서버 띄우기
#      python -m http.server 8000
#    - 같은 LAN의 갤럭시탭 브라우저로 http://PC_IP:8000/viewer.html (반출 화면)
#                                          또는 /pwa/scanner.html (스캐너)
#    - 스캐너: 헤더(seq=0) → 데이터(seq=1..N) 순서로 스캔 (순서 무관)
#    - 개발 모드: 카메라 없이 frames.json 텍스트 주입으로 검증 가능
#
#    ⚠ getUserMedia 는 secure context 필요:
#      - localhost (PC 자체) : HTTP 그대로 OK
#      - LAN IP 접속 시       : HTTPS 필요 (또는 Chrome flag
#        chrome://flags/#unsafely-treat-insecure-origin-as-secure 에
#        http://PC_IP:8000 등록)
#      - 카메라 없이 검증만이면 dev 모드 주입으로 충분
```

## 다음 단계

- [x] PWA 스캐너 페이지 구현 (decoder.mjs로 브라우저 포팅)
- [x] Windows AP 서버 점검 에이전트 (PowerShell)
- [ ] 갤럭시탭 실제 스캔 검증 (BarcodeDetector vs jsQR 인식률 비교, 거리/조도/글레어)
- [ ] SW Manager `/api/inspection/qr-batch` 엔드포인트 구현
- [ ] PWA 오프라인 캐싱 (service worker + jsQR 로컬 번들)
- [ ] AIX DB 에이전트 (ksh, 동일 컨트랙트)
