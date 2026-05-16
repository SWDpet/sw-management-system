# agent-windows/tools — PuTTY 번들 배포

이 디렉토리는 Windows 점검 에이전트가 사용하는 외부 바이너리(plink/pscp) 를 동봉하는 자리입니다. **파일은 git 추적 제외** (`.gitignore` 참조). 폐쇄망 배포 시 USB/메일로 같이 옮기세요.

## 왜 번들?

- Windows Server 2012 R2 (강진/단양 운영 환경) 는 OpenSSH 클라이언트 기본 미설치
- Server 2019+ 또는 Win10 가 아니면 `ssh.exe` 가 없거나 호환성 이슈
- PuTTY `plink.exe` 는 portable — 설치 없이 단일 exe 만 두면 동작
- `agent-windows/tools/plink.exe` 가 존재하면 `lib/Ssh.ps1` 의 `Resolve-SshClient` 가 PATH 보다 우선 사용

## 받는 곳

- 공식: https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html
- 64-bit Windows: `plink.exe`, `pscp.exe` 만 받으면 됨 (전체 PuTTY 설치 불필요)
- 라이선스: MIT (재배포 가능)
- 권장 버전: 0.78 이상 (`-pwfile` 옵션 등 보안 개선 포함)

## 배치 방법

```
agent-windows/
├─ tools/
│  ├─ plink.exe       ← SSH/Telnet 클라이언트 (필수)
│  ├─ pscp.exe        ← 파일 복사 (선택, Copy-RemoteFile 사용 시)
│  └─ README.md       ← 본 파일
├─ lib/
└─ ...
```

해시 검증 (PuTTY 0.78 64-bit):
- `plink.exe`: 사이트의 GPG-signed checksums 와 일치하는지 확인
- 폐쇄망 반입 시 보안팀 정책에 따라 별도 서명 절차 필요할 수 있음

## setup wizard 와의 연동

`setup.ps1` 의 protocol 선택 화면에서:
1. `Protocol`: `ssh` 또는 `telnet`
2. (ssh 일 때만) `SSH backend`: `auto` | `openssh` | `plink`
   - `auto`: bundled plink → ssh.exe (PATH) → PATH plink 순서로 자동 탐지
   - `openssh`: 강제로 `ssh.exe` 사용 (없으면 에러)
   - `plink`: 강제로 plink 사용 (bundled → config plink_path → PATH 순)
3. `telnet`: 백엔드 선택 없음. `lib/Telnet.ps1` 의 TcpClient 자체 구현 사용 (plink-telnet 은 auto-auth 미지원이라 채택 안 함)

## 보안 주의

- 현재 plink 분기는 password 를 `-pw` 인자로 전달 — 프로세스 인자가 `tasklist` 에 노출됨
- 폐쇄망 PoC 한정으로 허용. 운영 전환 시:
  - SSH 키 인증(`-i`) 으로 마이그레이션 (가장 안전)
  - 또는 plink 0.74+ 의 `-pwfile <path>` 사용 (인자 대신 임시파일에서 읽음)
- 호스트키 검증: `-batch` 로 미등록 호스트 거부. 첫 연결 전 `plink -ssh <host>` 로 키 등록 (호스트 키 fingerprint 수기 검증) 필요

## 트러블슈팅

- 동봉된 plink 가 사용 안 됨 → `lib/Ssh.ps1:_FindBundledTool` 가 `Join-Path (Split-Path -Parent $PSScriptRoot) 'tools'` 로 경로 빌드. agent-windows 루트 기준 `tools/plink.exe` 가 맞는지 확인.
- DPAPI 패스워드 디코딩 실패 → `lib/DPAPI.ps1` 의 `Unprotect-Password` 는 **암호화한 Windows 계정에서만 복호화 가능**. 점검자 계정 다르면 setup 재실행해서 재암호화 필요.
