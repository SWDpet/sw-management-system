<#
.SYNOPSIS
  GSS (GeoNURIS Spatial Server) 경로 자동 탐지 — 로컬 Windows 우선, UNIX 원격 fallback.

.DESCRIPTION
  사이트마다 GSS 설치 경로가 다름 (Windows AP / UNIX DB 양쪽). ps -ef|grep GSS 또는
  ps -ef|grep java 결과에서 동적으로 경로 추출.

  FR-2 (inspection-agent-v2-setup):
   1. 로컬 Windows: Test-Path $Config.paths.gss_log_dir (기존 v1 동작)
   2. 원격 UNIX (remotes.unix_db.enabled): ps -ef|grep 으로 추출
   3. 결과 캐시: 두 번째 실행부터 재탐지 안 함 (-ForceRescan 으로 강제)
#>

# ──────────────────────────────────────────────────────────────────────────
# 순수 함수 — ps 출력에서 GSS 경로 추출 (네트워크/파일시스템 의존성 없음, unit-test 가능)
# ──────────────────────────────────────────────────────────────────────────

function Find-GssPathFromPsOutput {
    <#
    .SYNOPSIS
      ps -ef | grep ... 결과 한 줄 이상에서 GSS 디렉토리 후보를 찾는다.

    .PARAMETER PsOutput
      ps -ef 결과 텍스트 (여러 줄 가능).

    .PARAMETER SearchHint
      'GSS' (기본) | 'java' — grep 종류 표기용 (detected_via 에 기록).

    .OUTPUTS
      [PSCustomObject] { gss_root, log_dir, detected_via }  또는  $null (못 찾음).
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)][AllowEmptyString()][string] $PsOutput,
        [string] $SearchHint = 'GSS'
    )
    if ([string]::IsNullOrWhiteSpace($PsOutput)) { return $null }

    # 1순위: GSS 키워드 포함 절대 경로 (예: /home/oracle/GeoNURIS_Spatial_Server/bin/gss-server)
    #        Spatial_Server | GeoNURIS | GSS 키워드
    $gssPathRegex = '(/[^\s]*(?:Spatial_Server|GeoNURIS_GSS|GeoNURIS_Spatial|GSS)[^\s]*)'
    $matches = [regex]::Matches($PsOutput, $gssPathRegex, 'IgnoreCase')
    if ($matches.Count -gt 0) {
        # 가장 빈도 높은 경로의 부모 디렉토리 추출
        $candidates = $matches | ForEach-Object { $_.Groups[1].Value }
        $gssRoot = _ExtractGssRoot -Paths $candidates
        if ($gssRoot) {
            return [PSCustomObject]@{
                gss_root     = $gssRoot
                log_dir      = "$gssRoot/logs"
                detected_via = "ps-grep-$SearchHint"
            }
        }
    }

    # 2순위: Tomcat catalina.home (-Dcatalina.home=/opt/tomcat) 또는 java 설치 경로 — GSS 가 Tomcat 의 webapps 일 수도
    $catalinaRegex = '-D(?:catalina\.home|catalina\.base)=([^\s]+)'
    $catMatch = [regex]::Match($PsOutput, $catalinaRegex)
    if ($catMatch.Success) {
        $tomcatHome = $catMatch.Groups[1].Value.TrimEnd('/')
        return [PSCustomObject]@{
            gss_root     = $tomcatHome
            log_dir      = "$tomcatHome/logs"
            detected_via = "ps-grep-$SearchHint-catalina"
        }
    }

    return $null
}

function _ExtractGssRoot {
    <# 여러 경로 후보 중 GSS 루트 디렉토리 추정. Spatial_Server 등 키워드가 마지막 segment 인 부모를 우선. #>
    param([string[]] $Paths)
    if (-not $Paths -or $Paths.Count -eq 0) { return $null }

    $rootCandidates = @()
    foreach ($p in $Paths) {
        $segments = $p.TrimEnd('/').Split('/')
        for ($i = $segments.Length - 1; $i -ge 0; $i--) {
            if ($segments[$i] -match '(?i)(Spatial_Server|GeoNURIS_GSS|GeoNURIS_Spatial)') {
                $rootCandidates += ($segments[0..$i] -join '/')
                break
            }
        }
    }
    if ($rootCandidates.Count -eq 0) {
        # 키워드가 segment 끝 아닌 어딘가에 있는 경우 — 처음 매칭된 경로의 디렉토리만 반환
        $first = $Paths[0]
        $dir = Split-Path -Parent $first
        if ($dir) { return $dir.Replace('\', '/') }
        return $null
    }
    # 가장 빈도 높은 후보
    $best = ($rootCandidates | Group-Object | Sort-Object Count -Descending | Select-Object -First 1).Name
    return $best
}

# ──────────────────────────────────────────────────────────────────────────
# 통합 탐지 — 로컬 + 원격 fallback + 캐시
# ──────────────────────────────────────────────────────────────────────────

function Find-GssPath {
    <#
    .SYNOPSIS
      통합 GSS 경로 탐지. 캐시 → 로컬 → 원격 fallback 순서.

    .PARAMETER Config
      site config 객체 (paths.gss_log_dir, remotes.unix_db 참조).

    .PARAMETER InvokeRemote
      원격 명령 실행 scriptblock. 시그니처: [scriptblock] { param($Remote, $Command, $TimeoutSec) → {ok, stdout, stderr, exitCode} }
      미지정 시 원격 fallback 시도 안 함 (로컬 결과만 반환).

    .PARAMETER ForceRescan
      캐시 무시 + 재탐지.

    .OUTPUTS
      [PSCustomObject] {
        gss_root      : "/home/oracle/GeoNURIS_Spatial_Server" 또는 "C:\GeoNURIS_Spatial_Server"
        log_dir       : "$gss_root/logs"
        detected_via  : "local" | "cache" | "ps-grep-GSS" | "ps-grep-java" | "ps-grep-GSS-catalina"
        detected_at   : ISO 8601 시각
        source        : "local" | "remote" | "cache"
      }
      또는 $null (탐지 실패 — 호출자가 처리).
    #>
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] $Config,
        [scriptblock] $InvokeRemote,
        [switch] $ForceRescan
    )

    # 1. 캐시 확인
    if (-not $ForceRescan -and $Config.PSObject.Properties['_gss_cache'] -and $Config._gss_cache.gss_root) {
        $c = $Config._gss_cache
        return [PSCustomObject]@{
            gss_root     = $c.gss_root
            log_dir      = $c.log_dir
            detected_via = $c.detected_via + '+cache'
            detected_at  = $c.detected_at
            source       = 'cache'
        }
    }

    # 2. 로컬 Windows 우선
    $localPath = $null
    if ($Config.paths -and $Config.paths.gss_log_dir) {
        $localPath = $Config.paths.gss_log_dir
    }
    if ($localPath -and (Test-Path $localPath)) {
        $localRoot = Split-Path -Parent $localPath
        return [PSCustomObject]@{
            gss_root     = $localRoot
            log_dir      = $localPath
            detected_via = 'local'
            detected_at  = (Get-Date).ToString('o')
            source       = 'local'
        }
    }

    # 3. 원격 fallback
    if (-not $InvokeRemote) { return $null }
    if (-not $Config.remotes -or -not $Config.remotes.PSObject.Properties['unix_db']) { return $null }
    $unixDb = $Config.remotes.unix_db
    if (-not $unixDb.enabled) { return $null }

    # 3-a. ps -ef | grep GSS
    $cmdGss = "ps -ef | grep -i 'spatial_server\|GeoNURIS\|GSS' | grep -v grep"
    $res = & $InvokeRemote $unixDb $cmdGss 15
    if ($res -and $res.ok) {
        $found = Find-GssPathFromPsOutput -PsOutput $res.stdout -SearchHint 'GSS'
        if ($found) {
            return [PSCustomObject]@{
                gss_root     = $found.gss_root
                log_dir      = $found.log_dir
                detected_via = $found.detected_via
                detected_at  = (Get-Date).ToString('o')
                source       = 'remote'
            }
        }
    }

    # 3-b. ps -ef | grep java (Tomcat fallback)
    $cmdJava = "ps -ef | grep java | grep -v grep"
    $res2 = & $InvokeRemote $unixDb $cmdJava 15
    if ($res2 -and $res2.ok) {
        $found = Find-GssPathFromPsOutput -PsOutput $res2.stdout -SearchHint 'java'
        if ($found) {
            return [PSCustomObject]@{
                gss_root     = $found.gss_root
                log_dir      = $found.log_dir
                detected_via = $found.detected_via
                detected_at  = (Get-Date).ToString('o')
                source       = 'remote'
            }
        }
    }

    return $null
}
