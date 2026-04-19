---
tags: [setup, obsidian, guide]
---

# 🗂 Obsidian 셋업 가이드 (신규 PC / 사무실 동기화)

> 이 저장소는 `docs/` 폴더를 Obsidian Vault 로 사용합니다. 아래 순서대로 따라하면
> 어느 PC 에서든 동일한 노트·플러그인·테마·단축키 환경이 재현됩니다.
>
> **대상**: 집 ↔ 사무실, 새 노트북/데스크탑, 팀 신규 합류자.

---

## 📌 핵심 원칙 (먼저 읽어주세요)

1. **Vault 경로 = 저장소 루트의 `docs/` 폴더**. 절대 다른 위치 사용 금지 (팀 설정 재현 불가).
2. **`docs/.obsidian/` 일부 파일만 git 에 포함** — 개인 레이아웃(`workspace.json`)은 제외, 공유 설정(플러그인·테마·단축키)은 포함.
3. **노트 수정 = git commit/push 로 동기화**. Obsidian 자체엔 sync 기능 없음 (유료 Obsidian Sync 는 사용 안 함).
4. 공유 보관함 컨벤션은 [[HOME]] + [[audit/dashboard]] 을 기점으로 탐색.

---

## 1. 사전 준비물

| 항목 | 설치/준비 |
|------|-----------|
| Git | https://git-scm.com/download/win 또는 `winget install --id Git.Git -e` |
| GitHub 접근 권한 | 이 저장소 (SWDpet/sw-management-system) 로의 clone 권한. 팀장에게 요청 |
| 저장소 SSH 키 또는 PAT | `git config --global ...` 또는 GitHub Desktop 로그인 |
| (선택) Claude Code | 문서·코드 수정 병행 시 |

---

## 2. 저장소 클론 (신규 PC 첫 설치)

```powershell
# 원하는 작업 디렉터리에서 (예: C:\Users\<본인>\)
git clone https://github.com/SWDpet/sw-management-system.git
cd sw-management-system
```

> 기존 PC 에서 이어 쓰는 거면 `git pull` 만 하면 됩니다.

---

## 3. 환경변수 설정 (앱 실행 필요 시)

서버를 돌려야 하는 PC 라면 Windows 사용자 환경변수에 `DB_PASSWORD` 등록 필수.
자세한 내용은 [[../DEPLOYMENT_GUIDE|DEPLOYMENT_GUIDE]] 참조. 노트만 볼 PC 라면 건너뛰어도 됩니다.

---

## 4. Obsidian 설치

### Windows 10/11 권장
```powershell
winget install --id Obsidian.Obsidian -e
```
또는 https://obsidian.md/download → Windows installer 64-bit.

macOS: `brew install --cask obsidian`
Linux: AppImage 다운로드.

---

## 5. Vault 열기 (⭐ 핵심 단계)

1. Obsidian 실행 → 초기 화면에서 **"폴더를 보관함으로 열기"** (Open folder as vault) 선택
2. 경로 지정:
   ```
   <저장소 클론 위치>\sw-management-system\docs
   ```
   예: `C:\Users\ukjin\sw-management-system\docs`
3. **"Trust author and enable plugins?"** 창이 뜨면 → **Trust** 클릭
   (이 저장소의 `.obsidian/` 에 저장된 플러그인 설정을 그대로 사용하기 위함)

### 전환되었는지 확인
왼쪽 파일 트리에 아래가 모두 보여야 성공:
- `HOME.md`
- `audit/dashboard.md`
- `plans/`, `dev-plans/`, `templates/` 폴더
- `ERD.md`, `erd-*.mmd` 다수

---

## 6. 플러그인 설치 & 활성화

`.obsidian/community-plugins.json` 에 **활성화할 플러그인 목록**이 저장되어 있습니다.
플러그인 바이너리 자체는 저장소에 포함되어 있어 **즉시 사용 가능**합니다.

### 확인 방법
1. `Ctrl + ,` (설정) → **커뮤니티 플러그인** (Community plugins)
2. **설치된 플러그인** 목록 확인:
   - **Dataview** — 활성
   - **Templater** — 활성 (설치되어 있을 시)
   - **Tag Wrangler** — 활성 (설치되어 있을 시)
3. 비활성화된 게 있으면 토글 ON.

### 플러그인 파일이 안 보이면
- 저장소를 **끝까지** pull 했는지 확인 (`.obsidian/plugins/` 폴더 존재 여부)
- 없으면: 설정 → 커뮤니티 플러그인 → **탐색** → 각 플러그인 검색해서 수동 설치 (이 경우 그냥 이름만 참고 — 설정은 저장소에서 자동 반영됨)

---

## 7. 동기화 워크플로우 (매일)

### PC 작업 시작 시
```powershell
cd C:\Users\<본인>\sw-management-system
git pull
```
→ 다른 PC 에서 남긴 노트 변경 반영.

### 작업 도중
- Obsidian 에서 자유롭게 편집. 파일은 `.md` 로 즉시 저장됨.
- 큰 변경은 수시로 commit 권장:
  ```powershell
  git add docs/
  git commit -m "note: <내용 요약>"
  ```

### 작업 종료 전
```powershell
git add -A
git commit -m "sync: 노트 정리 $(date +%Y-%m-%d)"
git push
```

### 충돌(conflict) 발생 시
- 같은 파일을 두 PC 에서 동시에 수정했을 때. 드물지만 발생 가능.
- `git status` 로 충돌 파일 확인 → Obsidian 에서 직접 열어 수동 병합 (`<<<<<<<` 마커 제거).
- 자신 없으면 변경 전 백업: `cp <file>.md <file>.md.bak`.

---

## 8. 공유 설정 vs 개인 설정

### ✅ 저장소에 커밋되는 항목 (팀 공유)
| 파일 | 내용 |
|------|------|
| `docs/.obsidian/app.json` | 앱 기본 설정 (ex: 편집기 너비) |
| `docs/.obsidian/appearance.json` | 테마 |
| `docs/.obsidian/core-plugins.json` | 활성화된 코어 플러그인 |
| `docs/.obsidian/community-plugins.json` | 활성화된 커뮤니티 플러그인 목록 |
| `docs/.obsidian/graph.json` | 그래프 뷰 기본 필터/스타일 |
| `docs/.obsidian/hotkeys.json` | 공유 단축키 (있으면) |
| `docs/.obsidian/plugins/*/` | 플러그인 바이너리·기본 설정 |

### ❌ 저장소에서 제외되는 항목 (개인)
| 파일 | 이유 |
|------|------|
| `docs/.obsidian/workspace.json` | 개인 창 레이아웃 — PC 별 상이 |
| `docs/.obsidian/workspace-mobile.json` | 모바일 레이아웃 |
| `docs/.obsidian/workspace.json.bak` | 자동 백업 |
| `docs/_private/` | 개인 메모 전용 디렉터리 (사용 시) |

`.gitignore` 의 `# Obsidian` 섹션 참조.

---

## 9. 자주 겪는 이슈

### Q. 두 PC 에서 workspace 가 계속 바뀌어요
A. `workspace.json` 은 gitignore 에 이미 있어 커밋 안 됩니다. 정상입니다. 각 PC 마다 창 배치가 다른 게 정상.

### Q. 플러그인 설정을 한 PC 에서 바꿨는데 다른 PC 에 안 와요
A. 해당 플러그인의 `.obsidian/plugins/<plugin>/data.json` 이 변경됐을 것. `git add` 해서 push 하면 전파됩니다.

### Q. "Trust author" 팝업이 계속 떠요
A. Obsidian 이 `.obsidian/` 의 외부 출처(git pull) 변경을 감지한 것. 한 번 Trust 하면 됩니다.

### Q. 상용 Obsidian Sync 안 써도 괜찮나요?
A. 네. git 기반 동기화가 이 저장소의 단일 경로입니다. Obsidian Sync 는 오히려 git 과 충돌할 수 있어 **사용 금지**.

### Q. 개인 메모는 어디에 저장하나요?
A. `docs/_private/` 를 만들어 사용. `.gitignore` 에 등록되어 있어 절대 커밋 안 됨. Obsidian 에서는 보임.

### Q. 모바일에서도 사용 가능한가요?
A. 가능하지만 공식적으로 지원하지 않습니다. 옵션:
- **Obsidian Mobile** + **Obsidian Git** (커뮤니티 플러그인) — iOS/Android 에서 git pull/push
- 또는 공식 **Obsidian Sync** (유료, 월 $4~)

---

## 10. 체크리스트 (신규 PC)

```
[ ] git clone 완료
[ ] docs/ 폴더 존재 확인 (HOME.md 포함)
[ ] Obsidian 설치
[ ] Vault 경로 = <repo>/docs 로 열기
[ ] "Trust author" 클릭
[ ] Community plugins 에서 Dataview 활성 확인
[ ] audit/dashboard.md 열어서 Dataview 표 정상 렌더 확인
[ ] HOME.md 에서 주요 문서 링크 클릭 테스트
[ ] 테스트 커밋·푸시 1회 (예: 이 체크리스트의 체크박스 켜기)
```

---

## 🔗 관련 문서

- [[HOME|문서 허브]]
- [[audit/dashboard|감사 조치 대시보드]]
- [[../CLAUDE|Claude Code 협업 가이드]]
- [[../DEPLOYMENT_GUIDE|앱 배포 가이드]]
