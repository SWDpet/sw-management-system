<#
.SYNOPSIS
  외부에서 만들어진 snapshot.json 을 QR 프레임으로 인코딩 (옵션 A 파이프라인).
.DESCRIPTION
  agent-unix (AIX/HP-UX/Linux/Solaris) 가 만든 snapshot.json 을 Windows 모니터링 PC 로
  옮겨와서 QR 화면 표시용 frames.json + summary.html 을 생성한다.
  agent-windows 의 QrPayload.ps1 / Common.ps1 을 재사용.
.PARAMETER InputPath
  snapshot/v1 형식 JSON 파일 경로.
.PARAMETER OutDir
  frames.json + summary.html 출력 디렉토리 (default: 같은 폴더 ./out)
.EXAMPLE
  .\encode-snapshot.ps1 -InputPath agent-unix\out\snapshot.json -OutDir out\unix-encoded
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string] $InputPath,
    [string] $OutDir
)

$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$libDir = Join-Path $root 'agent-windows\lib'

. (Join-Path $libDir 'Common.ps1')
. (Join-Path $libDir 'QrPayload.ps1')

if (-not (Test-Path $InputPath)) { throw "input not found: $InputPath" }
if (-not $OutDir) { $OutDir = Join-Path $root 'out_unix_encoded' }
if (-not (Test-Path $OutDir)) { New-Item -ItemType Directory -Path $OutDir | Out-Null }

$snapshot = Read-Json -Path $InputPath
Write-Log -Level INFO -Msg ("input  : {0}  ({1} items)" -f $InputPath, $snapshot.items.Count)
Write-Log -Level INFO -Msg ("site={0} tier={1} host={2}" -f $snapshot.site, $snapshot.tier, $snapshot.host.hostname)

$qrPayload = ConvertTo-QrPayload -Snapshot $snapshot
$qr = Build-QrFrames -QrPayload $qrPayload -MaxChunkChars 1800

$framesPath = Join-Path $OutDir 'frames.json'
Write-Json -Object $qr.Frames -Path $framesPath

Write-Log -Level INFO -Msg ("frames : {0}" -f $framesPath)
Write-Log -Level INFO -Msg ("  total={0}  raw={1}B  gz={2}B  b45={3}c  sha={4}" -f `
    $qr.Frames.Count, $qr.RawBytes, $qr.GzBytes, $qr.B45Chars, $qr.Hash)
