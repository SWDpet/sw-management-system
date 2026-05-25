# UTF-8 BOM 인코딩 변환 — AP 서버 (Windows Server 2012 R2 / PS 4.0) 용
# 사용: D:\GJ\inspection-poc\agent-windows 에서 .\fix-encoding.ps1 실행
$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$utf8Bom = New-Object System.Text.UTF8Encoding($true)
$fixed = 0
Get-ChildItem $base -Recurse -Include *.ps1,*.json | ForEach-Object {
    $raw = [System.IO.File]::ReadAllBytes($_.FullName)
    if ($raw.Length -ge 3 -and $raw[0] -eq 0xEF -and $raw[1] -eq 0xBB -and $raw[2] -eq 0xBF) { return }
    $text = [System.Text.Encoding]::UTF8.GetString($raw)
    [System.IO.File]::WriteAllText($_.FullName, $text, $utf8Bom)
    Write-Host "FIXED: $($_.FullName.Substring($base.Length))"
    $script:fixed++
}
Write-Host "`nDone: $fixed files converted to UTF-8 BOM"
