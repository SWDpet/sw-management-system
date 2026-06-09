# GIT_REMOTE_SETUP.md — git 신원 · 듀얼 저장소 푸시 런북

> 모든 머신(집 PC / 회사 PC / 출장지 / 운영서버)에서 동일하게 맞춰야 하는 git 신원·푸시 설정.
> 이 문서는 **무엇을·왜·어떻게**를 공유한다. 실제 설정 명령은 각 머신에서 1회 직접 실행해야 한다(토큰은 머신별 로컬).

---

## 1. 저장소 ↔ 계정 ↔ 이메일

본 코드는 **두 개의 GitHub 저장소**에 미러링된다.

| 저장소 | 소유 계정 | 연결 이메일 | 역할 |
|---|---|---|---|
| `SWDpet/sw-management-system` | 회사 조직 (소유주 `ukjin_park@jungdouit.com`) | `ukjin_park@jungdouit.com` | **주(primary)·최신** (origin fetch) |
| `ukjin914/sw-management-system` | 개인 `ukjin914` | `ukjin55@gmail.com` | 개인 미러/백업 |

- 개인 master 는 origin 의 엄밀한 조상 → 갈라짐 없이 항상 FF 로 따라잡힘. 안 밀면 개인 쪽이 뒤처질 뿐.

---

## 2. 커밋 신원 (모든 머신 공통)

```bash
git config --global user.email "ukjin_park@jungdouit.com"
git config --global user.name  "ukjin914"
```

- **author 이메일은 `ukjin_park@jungdouit.com` 로 통일.** 한 커밋은 양쪽 저장소에 동일 객체로 들어가므로 저장소별로 다른 이메일을 박을 수 없다.
- ⚠ **운영서버 PC 주의**: 과거 운영서버 세션이 `ukjin00@gmail.com`(개인도 회사도 아닌 잘못된 stray)으로 커밋한 적 있음(예: `2a95475`). 운영서버 PC 에서 작업 전 위 `git config` 를 반드시 확인·교정할 것.

---

## 3. 한 번에 두 저장소로 푸시 (머신별 1회 설정)

목표: `git push origin master` 한 번 = SWDpet + 개인 양쪽 푸시.

### 3-1. 푸시 권한 함정 (왜 토큰을 경로별로 매칭해야 하나)

| 토큰 | SWDpet push | 개인 ukjin914 push |
|---|---|---|
| SWDpet PAT (조직 쓰기권한) | ✅ | ❌ 403 |
| `ukjin914` gh 토큰 (`gh auth token`) | ❌ (pull만) | ✅ admin |

→ 한 토큰으로 둘 다 못 민다. URL(경로)별로 맞는 토큰을 써야 한다.

### 3-2. 설정 명령

```bash
# (1) origin 에 푸시 URL 2개 등록
git remote set-url --add --push origin https://github.com/SWDpet/sw-management-system.git
git remote set-url --add --push origin https://github.com/ukjin914/sw-management-system.git

# (2) 이 저장소만 경로 기반 자격증명 사용
git config credential.useHttpPath true
```

(3) **경로별 자격증명 저장** — OS 자격증명 store 에 아래 2개를 넣는다. 토큰은 머신별 로컬이며 **절대 커밋·공유 금지**.

- `https://github.com/SWDpet/sw-management-system.git` → SWDpet 조직 PAT
- `https://github.com/ukjin914/sw-management-system.git` → `gh auth token` (ukjin914)

저장 방법(택1):
- 그냥 `git push origin master` 한 번 실행 → 자격증명 관리자가 각 URL 마다 로그인 프롬프트 → 맞는 계정으로 인증하면 store 에 저장됨.
- 또는 `git credential approve` 로 직접 주입 (Windows PowerShell 은 stdin 인코딩 문제로 `cmd /c "git credential approve < 임시파일"` 방식 권장. `| git credential approve` 파이프는 "missing protocol field" 로 실패함).

### 3-3. 검증

```bash
git push origin master          # SWDpet up-to-date + 개인 FF
# 양쪽 SHA 일치 확인
gh api repos/SWDpet/sw-management-system/commits/master --jq '.sha[0:7]'
gh api repos/ukjin914/sw-management-system/commits/master --jq '.sha[0:7]'
```

---

## 4. 결정 사항 (history)

- **2026-06-09**: stray 커밋 `2a95475`(author=`ukjin00@gmail.com`)는 **정정하지 않음**(사용자 결정). 이미 양쪽 public·동기화된 단일 커밋이라 amend+force-push 의 SHA 변경·병렬세션 충돌 위험이 실익보다 큼. 앞으로 이 커밋 건드리지 말 것. 재발 방지는 §2 의 운영서버 PC config 교정으로 한다.

---

*관련: `docs/DB_CONNECTION.md`(환경별 접속), `docs/AGENT_SAFETY.md`(시크릿 안전 채널).*
