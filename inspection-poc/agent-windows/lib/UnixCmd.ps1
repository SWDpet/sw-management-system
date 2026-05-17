<#
.SYNOPSIS
  Unix DB 서버 점검용 OS 별 명령 lookup 테이블.

.DESCRIPTION
  agent-unix/checks/{aix,linux,solaris,hpux}/*.sh 의 RAW 명령 부분만 추출.
  파싱은 PS 측 (DbOsCheck.ps1) 에서 수행 — Telnet 명령 길이 짧게 유지, escape 위험 회피.

  모든 명령은 single-quoted PS 문자열로 보관해서 $ escape 문제 차단.
#>

. (Join-Path $PSScriptRoot 'Common.ps1')

$script:UnixCmdTable = @{
    'cpu_info' = @{
        aix     = 'prtconf 2>/dev/null | egrep "Number Of Processors|Processor Type|Processor Clock"'
        linux   = 'lscpu 2>/dev/null || cat /proc/cpuinfo | egrep "model name|cpu MHz|^processor" | head -20'
        solaris = 'psrinfo -v 2>/dev/null | head -20'
        hpux    = 'machinfo 2>/dev/null | egrep -i "processor|cpu"'
    }
    'mem_info' = @{
        aix     = 'prtconf 2>/dev/null | grep -i "Memory Size"'
        linux   = 'free -m 2>/dev/null'
        solaris = 'prtconf 2>/dev/null | grep -i "Memory size"'
        hpux    = 'machinfo 2>/dev/null | grep -i memory'
    }
    'perf_cpu' = @{
        aix     = 'vmstat 1 2 2>/dev/null | tail -1'
        linux   = 'vmstat 1 2 2>/dev/null | tail -1'
        solaris = 'vmstat 1 2 2>/dev/null | tail -1'
        hpux    = 'vmstat 1 2 2>/dev/null | tail -1'
    }
    'perf_mem' = @{
        # head -10 으로 늘림 — 'Unit:' 헤더 + dash 구분선 + 컬럼명 + memory 행 + pg space 행
        # (head -3 은 헤더만 잡고 memory 행 못 잡았음 — 2026-05-17 강진 라운드 확인)
        aix     = 'svmon -G -O unit=MB 2>/dev/null | head -10'
        linux   = 'free -m 2>/dev/null'
        solaris = 'vmstat 1 2 2>/dev/null | tail -1'
        hpux    = 'swapinfo -m 2>/dev/null'
    }
    'disk' = @{
        # AIX 일부 버전에서 'df -gH' 옵션 거부 → 빈 stdout (2026-05-17 강진 라운드 확인).
        # df -g (GB 단위) 만 사용. -H 는 human-readable 인데 -g 와 충돌. -g 단독으로 충분.
        aix     = 'df -g 2>/dev/null'
        linux   = 'df -PT 2>/dev/null'
        solaris = 'df -h 2>/dev/null'
        hpux    = 'bdf 2>/dev/null'
    }
    'network_ip' = @{
        aix     = 'ifconfig -a 2>/dev/null | egrep "^[a-z]|inet "'
        linux   = 'ip -4 -o addr show 2>/dev/null || ifconfig -a | egrep "^[a-z]|inet "'
        solaris = 'ifconfig -a4 2>/dev/null'
        hpux    = 'netstat -ni 2>/dev/null'
    }
    'users' = @{
        aix     = 'who 2>/dev/null'
        linux   = 'who 2>/dev/null'
        solaris = 'who 2>/dev/null'
        hpux    = 'who 2>/dev/null'
    }
    'uptime' = @{
        aix     = 'uptime 2>/dev/null'
        linux   = 'uptime 2>/dev/null'
        solaris = 'uptime 2>/dev/null'
        hpux    = 'uptime 2>/dev/null'
    }
    # ── inspect-report-d-v6 Phase A1: DB OS 7건 신규 키 ───────────────────
    'adapter' = @{
        aix     = 'lsdev -Cc adapter 2>/dev/null'
        linux   = 'lspci 2>/dev/null | head -20'
        solaris = 'cfgadm -a 2>/dev/null | head -20'
        hpux    = 'ioscan -fn 2>/dev/null | head -30'
    }
    'swap' = @{
        aix     = 'lsps -a 2>/dev/null'
        linux   = 'free -m 2>/dev/null | grep -i swap'
        solaris = 'swap -s 2>/dev/null'
        hpux    = 'swapinfo -m 2>/dev/null'
    }
    'iostat' = @{
        # 1 초 간격 2회 샘플 — 첫 샘플은 평균이라 버리고 마지막 사용
        aix     = 'iostat 1 2 2>/dev/null | tail -20'
        linux   = 'iostat 1 2 2>/dev/null | tail -20'
        solaris = 'iostat 1 2 2>/dev/null | tail -20'
        hpux    = 'iostat 1 2 2>/dev/null | tail -20'
    }
    'netstat_perf' = @{
        # 인터페이스별 패킷 통계 — Ierrs/Oerrs 컬럼이 네트워크 성능 지표
        aix     = 'netstat -ni 2>/dev/null'
        linux   = 'netstat -i 2>/dev/null'
        solaris = 'netstat -ni 2>/dev/null'
        hpux    = 'netstat -i 2>/dev/null'
    }
    'inode' = @{
        aix     = 'df -i 2>/dev/null'
        linux   = 'df -i 2>/dev/null'
        solaris = 'df -F ufs -o i 2>/dev/null'
        hpux    = 'bdf -i 2>/dev/null'
    }
    'lsvg' = @{
        # rootvg LV/PV 상태 — AIX 전용. 다른 OS 는 lvdisplay 대체.
        aix     = 'lsvg -l rootvg 2>/dev/null'
        linux   = 'lvdisplay 2>/dev/null | head -30'
        solaris = 'zpool status rpool 2>/dev/null'
        hpux    = 'vgdisplay -v vg00 2>/dev/null | head -30'
    }
    # network-health 는 1 module → 3 row (Link/Ping/Collisions) — ps1 안에서 3개 키 호출
    'net_link' = @{
        aix     = 'netstat -na 2>/dev/null | head -20'
        linux   = 'ss -s 2>/dev/null'
        solaris = 'netstat -na 2>/dev/null | head -20'
        hpux    = 'netstat -na 2>/dev/null | head -20'
    }
    'net_ping' = @{
        # gateway IP 자동 추출 후 ping 1회 — 응답시간 확인
        aix     = 'GW=$(netstat -rn 2>/dev/null | awk "/^default/{print \$2; exit}") && ping -c 1 -w 3 $GW 2>/dev/null || echo "no gateway"'
        linux   = 'GW=$(ip route show default 2>/dev/null | awk "{print \$3; exit}") && ping -c 1 -w 3 $GW 2>/dev/null || echo "no gateway"'
        solaris = 'GW=$(netstat -rn 2>/dev/null | awk "/^default/{print \$2; exit}") && ping -s $GW 56 1 2>/dev/null'
        hpux    = 'GW=$(netstat -rn 2>/dev/null | awk "/^default/{print \$2; exit}") && ping $GW 1 2>/dev/null'
    }
    'net_collisions' = @{
        # Coll 컬럼 (또는 Oerrs/Ierrs) — netstat_perf 와 동일 출력이나 parser 가 다른 컬럼 추출
        aix     = 'netstat -ni 2>/dev/null'
        linux   = 'netstat -i 2>/dev/null'
        solaris = 'netstat -ni 2>/dev/null'
        hpux    = 'netstat -i 2>/dev/null'
    }
}

# vmstat 의 idle 컬럼 — 끝에서 N번째 토큰 위치 (0 = 마지막).
# linux: ... us sy id wa st  → id = 끝에서 2 (st=0, wa=1, id=2)
# aix:   ... us sy id wa     → id = 끝에서 1 (wa=0, id=1)
# solaris: ... us sy id      → id = 끝에서 0
# hpux:  ... us sy id        → id = 끝에서 0
$script:VmstatIdleFromEnd = @{
    linux = 2; aix = 1; solaris = 0; hpux = 0
}

function Get-UnixCmd {
    param([string]$Key, [string]$Os)
    $os = if ($Os) { $Os.ToLower() } else { 'linux' }
    if (-not $script:UnixCmdTable.ContainsKey($Key)) { return $null }
    $row = $script:UnixCmdTable[$Key]
    if (-not $row.ContainsKey($os)) { return $null }
    return $row[$os]
}

function Get-VmstatIdleOffset {
    param([string]$Os)
    $os = if ($Os) { $Os.ToLower() } else { 'linux' }
    if ($script:VmstatIdleFromEnd.ContainsKey($os)) { return $script:VmstatIdleFromEnd[$os] }
    return 2   # default linux 패턴
}
