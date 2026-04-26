#Requires -Version 5.1
<#
.SYNOPSIS
    SW Manager 프로젝트 개발환경 자동 세팅 (Windows 10/11 전용)

.DESCRIPTION
    새 머신에서 이 프로젝트를 빌드·실행할 수 있는 상태까지 자동 세팅합니다.

    설치 항목 (winget / npm, Machine scope):
      - Eclipse Temurin JDK 17     (JAVA_HOME + PATH 자동 설정)
      - Node.js LTS                 (PATH 자동 설정)
      - OpenAI Codex CLI            (@openai/codex, global)
      - GitHub CLI                  (선택)

    환경변수 (User scope — 민감정보):
      - DB_PASSWORD                 (미설정 시 입력 프롬프트)
      - OPENAI_API_KEY              (미설정 시 입력 프롬프트)
      - codex login                 (API key로 ~/.codex/auth.json 등록, BOM 이슈 우회)

    동작 원칙:
      - 이미 설치된 항목은 건너뜀 (idempotent, -Force 로 재설치)
      - 입력은 SecureString (화면에 에코되지 않음)
      - 비밀값을 로그/화면에 출력하지 않음

.PARAMETER SkipInstall
    소프트웨어 설치 단계를 건너뛰고 상태 점검만 수행

.PARAMETER Force
    이미 설치된 항목도 재설치 / 재등록

.EXAMPLE
    .\scripts\dev-setup.ps1
    # 대화형 전체 세팅

.EXAMPLE
    .\scripts\dev-setup.ps1 -SkipInstall
    # 현재 세팅 상태만 점검

.NOTES
    관리자 권한이 필요한 단계(winget machine-scope 설치)에서는 UAC 프롬프트가 뜹니다.
    스크립트 자체는 일반 PowerShell 창에서 실행하면 됩니다.
#>
[CmdletBinding()]
param(
    [switch]$SkipInstall,
    [switch]$Force
)

# PS 5.1 + native 명령 stderr 호환: Stop 모드면 codex 등의 stderr 출력이 터미네이팅 에러로
# 처리되어 스크립트가 중단됨. Continue 로 두고 $LASTEXITCODE/출력 매칭으로 실패 판별.
$ErrorActionPreference = 'Continue'

# ----- 헬퍼 출력 -----
function Write-Section { param([string]$Title) Write-Host ""; Write-Host "=== $Title ===" -ForegroundColor Cyan }
function Write-Ok      { param([string]$Msg)   Write-Host "  [OK] $Msg"   -ForegroundColor Green }
function Write-Warn    { param([string]$Msg)   Write-Host "  [!!] $Msg"   -ForegroundColor Yellow }
function Write-Err     { param([string]$Msg)   Write-Host "  [XX] $Msg"   -ForegroundColor Red }
function Write-Step    { param([string]$Msg)   Write-Host "  -> $Msg"     -ForegroundColor Gray }

$script:ErrorCount = 0
function Note-Error { $script:ErrorCount++ }

# ----- 1. Preflight -----
Write-Section 'Preflight — 필수 도구 확인'

if (-not (Get-Command winget -ErrorAction SilentlyContinue)) {
    Write-Err "winget 미설치. Windows 10 1809+ / 11 필요."
    Write-Err "설치: https://apps.microsoft.com/detail/9NBLGGH4NNS1"
    exit 1
}
Write-Ok "winget: $((winget --version | Out-String).Trim())"

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Err "git 미설치. Git for Windows 먼저 설치: https://git-scm.com/download/win"
    exit 1
}
Write-Ok "git: $((git --version | Out-String).Trim())"

$bashAvailable = $null -ne (Get-Command bash.exe -ErrorAction SilentlyContinue)
if ($bashAvailable) {
    Write-Ok "bash.exe: 사용 가능 (codex login 우회에 필요)"
} else {
    Write-Warn "bash.exe 없음. Git for Windows 의 Git Bash 를 포함해 재설치 권장."
}

# ----- 2. JDK 17 -----
Write-Section 'JDK 17 (Eclipse Temurin)'

$jdkRoot = $null
if (Test-Path 'C:\Program Files\Eclipse Adoptium') {
    $jdkRoot = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like 'jdk-17*' } | Select-Object -First 1
}

if ($jdkRoot -and -not $Force) {
    Write-Ok "이미 설치됨: $($jdkRoot.FullName)"
} elseif ($SkipInstall) {
    Write-Warn "JDK 17 미설치 (SkipInstall 모드)"
    Note-Error
} else {
    Write-Step "winget install EclipseAdoptium.Temurin.17.JDK (Machine scope, ~5분)"
    winget install --id EclipseAdoptium.Temurin.17.JDK -e `
        --silent --accept-source-agreements --accept-package-agreements --scope machine
    if ($LASTEXITCODE -ne 0) {
        Write-Err "JDK 17 설치 실패 (exit=$LASTEXITCODE)"
        Note-Error
    } else {
        $jdkRoot = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory |
            Where-Object { $_.Name -like 'jdk-17*' } | Select-Object -First 1
        Write-Ok "설치 완료: $($jdkRoot.FullName)"
    }
}

$javaHomeMachine = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
if ($javaHomeMachine -and (Test-Path $javaHomeMachine)) {
    Write-Ok "JAVA_HOME (Machine): $javaHomeMachine"
} else {
    Write-Warn "JAVA_HOME (Machine) 미설정. MSI 재설치 시 자동 설정됩니다."
}

# ----- 3. Node.js -----
Write-Section 'Node.js LTS'

$nodeExe = 'C:\Program Files\nodejs\node.exe'
$nodeInstalled = Test-Path $nodeExe

if ($nodeInstalled -and -not $Force) {
    $nodeVer = (& $nodeExe --version | Out-String).Trim()
    Write-Ok "이미 설치됨: $nodeVer"
} elseif ($SkipInstall) {
    Write-Warn "Node.js 미설치 (SkipInstall 모드 — codex CLI 설치 불가)"
    Note-Error
} else {
    Write-Step "winget install OpenJS.NodeJS.LTS (Machine scope)"
    winget install --id OpenJS.NodeJS.LTS -e `
        --silent --accept-source-agreements --accept-package-agreements --scope machine
    if ($LASTEXITCODE -ne 0) {
        Write-Err "Node.js 설치 실패 (exit=$LASTEXITCODE)"
        Note-Error
    } else {
        Write-Ok "설치 완료"
        $nodeInstalled = Test-Path $nodeExe
    }
}

# 현재 세션 PATH 에 nodejs 추가 (이후 npm/codex 호출용)
if ($nodeInstalled) {
    $env:PATH = "C:\Program Files\nodejs;" + $env:PATH
}

# ----- 4. Codex CLI -----
Write-Section 'OpenAI Codex CLI'

$npmCmd = 'C:\Program Files\nodejs\npm.cmd'
$codexCmd = "$env:APPDATA\npm\codex.cmd"
$codexInstalled = Test-Path $codexCmd

if ($codexInstalled -and -not $Force) {
    $codexVer = (& $codexCmd --version 2>&1 | Out-String).Trim()
    Write-Ok "이미 설치됨: $codexVer"
} elseif (-not (Test-Path $npmCmd)) {
    Write-Warn "npm 없음 — codex CLI 설치 건너뜀"
    Note-Error
} else {
    Write-Step "npm install -g @openai/codex"
    & $npmCmd install -g '@openai/codex' 2>&1 | Out-String | Write-Host
    if (Test-Path $codexCmd) {
        Write-Ok "설치 완료"
        $codexInstalled = $true
    } else {
        Write-Err "codex 설치 실패"
        Note-Error
    }
}

# ----- 5. GitHub CLI (선택) -----
Write-Section 'GitHub CLI (선택)'

$ghExe = 'C:\Program Files\GitHub CLI\gh.exe'
if ((Test-Path $ghExe) -and -not $Force) {
    Write-Ok "이미 설치됨"
} elseif ($SkipInstall) {
    Write-Warn "gh CLI 미설치 (선택 사항)"
} else {
    Write-Step "winget install GitHub.cli"
    winget install --id GitHub.cli -e `
        --silent --accept-source-agreements --accept-package-agreements --scope machine 2>&1 | Out-Null
    if (Test-Path $ghExe) { Write-Ok "설치 완료" } else { Write-Warn "설치 건너뜀 (선택 사항이라 무시)" }
}

# ----- 6. 환경변수 (User scope) -----
Write-Section '환경변수 (User scope — 민감정보)'

function Get-OrPromptUserEnv {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][string]$PromptText
    )
    $existing = [Environment]::GetEnvironmentVariable($Name, 'User')
    if ($existing) {
        Write-Ok "$Name : 설정됨 (length=$($existing.Length))"
        return $existing
    }
    Write-Warn "$Name : 미설정"
    $secure = Read-Host -Prompt "    $PromptText (엔터로 건너뛰기)" -AsSecureString
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        $value = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
    if ([string]::IsNullOrWhiteSpace($value)) {
        Write-Warn "$Name : 입력 없음 — 건너뜀"
        return $null
    }
    [Environment]::SetEnvironmentVariable($Name, $value, 'User')
    Write-Ok "$Name : 저장 완료 (User 환경변수, 새 셸부터 반영)"
    return $value
}

$dbPwd  = Get-OrPromptUserEnv -Name 'DB_PASSWORD'    -PromptText 'DB_PASSWORD (postgres 계정 비밀번호)'
$apiKey = Get-OrPromptUserEnv -Name 'OPENAI_API_KEY' -PromptText 'OPENAI_API_KEY (sk-... 형식)'

# ----- 7. Codex API key 등록 -----
Write-Section 'Codex API key 등록 (~/.codex/auth.json)'

if (-not $codexInstalled) {
    Write-Warn "codex CLI 미설치 — 건너뜀"
} elseif (-not $apiKey) {
    Write-Warn "OPENAI_API_KEY 없음 — 건너뜀"
} else {
    # codex 는 정상 출력도 stderr 로 쓰기 때문에 PS 5.1 의 NativeCommandError wrap 을 피하려고
    # cmd.exe 를 거쳐 stderr→stdout 병합 후 받아온다.
    $loginStatus = (cmd /c "`"$codexCmd`" login status 2>&1" | Out-String).Trim()
    if ($loginStatus -match 'Logged in using an API key' -and -not $Force) {
        Write-Ok "이미 로그인됨 ($loginStatus)"
    } elseif (-not $bashAvailable) {
        Write-Err "bash.exe 없음 — PowerShell 직접 파이프는 UTF-16 BOM 이슈로 401 발생함. Git Bash 설치 후 재시도."
        Note-Error
    } else {
        Write-Step "Git Bash 로 API key 파이프 (BOM-free)"
        # env var 로 전달 → bash printf (trailing newline 없음) → codex login --with-api-key
        $env:OPENAI_API_KEY = $apiKey
        $result = (& bash -c 'printf "%s" "$OPENAI_API_KEY" | codex login --with-api-key' 2>&1 | Out-String).Trim()
        if ($result -match 'Successfully logged in') {
            Write-Ok "codex login 완료"
        } else {
            Write-Err "codex login 실패"
            Write-Host "    $result" -ForegroundColor DarkYellow
            Note-Error
        }
    }
}

# ----- 8. Maven 빌드 검증 -----
Write-Section 'Maven 빌드 검증'

# 프로젝트 루트 탐색 (스크립트 위치 기준 상위)
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $projectRoot 'mvnw.cmd'))) {
    # 한 단계 더 올라가보기 (scripts/dev-setup.ps1 이 아닌 다른 경로에서 실행 대비)
    $projectRoot = (Get-Location).Path
}

$mvnwCmd = Join-Path $projectRoot 'mvnw.cmd'
if (-not (Test-Path $mvnwCmd)) {
    Write-Warn "mvnw.cmd 를 $projectRoot 에서 찾지 못함 — 프로젝트 루트에서 실행하세요"
} elseif (-not $javaHomeMachine) {
    Write-Warn "JAVA_HOME 미설정 — mvnw 동작 확인 건너뜀"
} else {
    # 현재 세션에 JAVA_HOME + PATH 반영
    $env:JAVA_HOME = $javaHomeMachine
    $env:PATH = "$javaHomeMachine\bin;" + $env:PATH
    Write-Step "$mvnwCmd -v"
    Push-Location $projectRoot
    try {
        $mvnOut = (& $mvnwCmd -v 2>&1 | Out-String)
        if ($mvnOut -match 'Apache Maven' -and $mvnOut -match 'Java version: 17') {
            Write-Ok "Maven wrapper + JDK 17 동작 확인"
        } else {
            Write-Warn "Maven wrapper 응답 이상"
            Write-Host $mvnOut -ForegroundColor DarkGray
            Note-Error
        }
    } finally {
        Pop-Location
    }
}

# ----- 9. DB 접근 힌트 -----
Write-Section 'DB 접근성 (참고)'
Write-Host "  application.properties 기본 DB: 211.104.137.55:5881"
Write-Host "  사내망 전용이면 VPN 연결 필요. 접근 테스트:"
Write-Host "    Test-NetConnection 211.104.137.55 -Port 5881" -ForegroundColor DarkGray

# ----- 10. 요약 -----
Write-Section '세팅 요약'
if ($script:ErrorCount -eq 0) {
    Write-Host "  전체 단계 완료" -ForegroundColor Green
} else {
    Write-Host "  $script:ErrorCount 건의 경고/오류 발생 — 위 로그 확인" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "  다음 단계:"
Write-Host "    1. 이 PowerShell 창 닫고 새 창 열기 (Machine PATH/User 환경변수 반영)"
Write-Host "    2. 프로젝트 루트에서:  bash server-restart.sh"
Write-Host "    3. 브라우저:  http://localhost:9090"
Write-Host ""

exit $script:ErrorCount
