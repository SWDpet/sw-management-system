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
│   ├── decode.js             PWA 측 디코더 (CRC + 조립 + SHA-1 + gunzip)
│   └── fault_test.js         결함 주입 테스트 (CRC/누락/SHA 변조)
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

# 5. 갤럭시탭 검증
#    - inspection-poc/ 디렉토리에 간단한 HTTP 서버 띄우기
#      python -m http.server 8000
#    - 같은 LAN의 갤럭시탭 브라우저로 http://PC_IP:8000/viewer.html
#    - 카메라 앱으로 QR 스캔 → seq=0(헤더) → seq=1.. 순서로
```

## 다음 단계

- [ ] 갤럭시탭 실제 스캔 검증 (BarcodeDetector vs jsQR 인식률 비교)
- [ ] PWA 스캐너 페이지 구현 (decode.js를 브라우저로 포팅)
- [ ] SW Manager `/api/inspection/qr-batch` 엔드포인트 구현
- [ ] Windows AP 서버 점검 에이전트 (PowerShell)
