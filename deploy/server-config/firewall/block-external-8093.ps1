# block-external-8093.ps1
# 운영서버(DESKTOP-H9IKIOT) Tomcat 8093 외부 노출 차단 — 형상관리용 스냅샷.
#
# 배경:
#   Tomcat HTTP 커넥터(8093)가 0.0.0.0 으로 떠 있어 외부에서 직접 접근 가능했고,
#   인터넷 취약점 스캐너(ThinkPHP/PHPUnit/pearcmd RCE 등, UA "libredtail-http" 류)가
#   nginx(443 TLS 종단)를 우회해 8093 을 직격 → Http11 요청 파싱 에러
#   (java.lang.IllegalArgumentException: Invalid character found in the request target)를 유발했다.
#   nginx 는 127.0.0.1:8093 으로만 프록시하므로 8093 의 외부 노출은 불필요하다.
#
# 조치:
#   외부(Internet) → 8093 인바운드만 차단하고 localhost(nginx) 경로는 통과시킨다.
#   무중단(Tomcat 재시작 불필요). 멱등(있으면 재생성).
#
# 실행: 관리자 권한 PowerShell 필요.
#   powershell -NoProfile -ExecutionPolicy Bypass -File .\block-external-8093.ps1
#
# 근본책(선택, 재시작 필요): C:\tomcat11\conf\server.xml 의 8093 커넥터에
#   address="127.0.0.1" 를 추가하면 Tomcat 이 처음부터 외부에 바인딩하지 않는다.

$ErrorActionPreference = 'Stop'

$name = 'Block external 8093 (Tomcat)'

# 멱등: 동일 이름 규칙이 있으면 지우고 재생성
$existing = Get-NetFirewallRule -DisplayName $name -ErrorAction SilentlyContinue
if ($existing) {
    $existing | Remove-NetFirewallRule
    Write-Host "기존 규칙 제거 후 재생성합니다."
}

New-NetFirewallRule `
    -DisplayName $name `
    -Direction Inbound `
    -LocalPort 8093 `
    -Protocol TCP `
    -Action Block `
    -RemoteAddress Internet `
    -Profile Any | Out-Null

Write-Host "방화벽 규칙 적용 완료: '$name' (외부 차단 / localhost 통과)"

# 검증: localhost 경로(nginx)는 여전히 8093 에 닿아야 한다
$ok = (Test-NetConnection -ComputerName 127.0.0.1 -Port 8093 -WarningAction SilentlyContinue).TcpTestSucceeded
Write-Host "localhost:8093 접속(=nginx 경로) 정상 여부: $ok"
