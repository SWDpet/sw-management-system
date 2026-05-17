# inspection-poc/_archive/

활성 점검 시스템에서 분리한 자산 보관소. **agent-windows 가 active** 영역.

## agent-unix/ (이전 위치: inspection-poc/agent-unix/)

- **이전일**: 2026-05-17
- **사유**: agent-windows 가 SSH/Telnet 으로 원격 Unix DB 를 점검하는 구조로 일원화 (v0.3.0). agent-unix 직접 실행은 더 이상 안 함.
- **포함**: AIX/Linux/HPUX/Solaris shell 점검 스크립트 60+ 개, Oracle .sh 점검 17종, 단양군(dyg) snapshot 8건, oracle.sh/json.sh/common.sh lib.
- **복구 시나리오**: 단양군 (또는 다른 사이트) 이 Linux/AIX 에 직접 unix agent 실행 필요해지면 `mv _archive/agent-unix ../agent-unix` 로 복귀 후 사용.
