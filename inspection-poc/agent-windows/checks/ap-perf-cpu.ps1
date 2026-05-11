param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# 3 sample 평균 (단발 측정은 변동 크므로)
$samples = Get-Counter '\Processor(_Total)\% Processor Time' -SampleInterval 1 -MaxSamples 3 -ErrorAction SilentlyContinue
$avg = [math]::Round((($samples.CounterSamples | Measure-Object -Average CookedValue).Average), 1)

$th = $Config.thresholds.'perf.cpu_pct'
$status = Resolve-Status -Value $avg -Threshold $th

New-CheckResult `
    -Id 'ap.perf.cpu_pct' `
    -Label 'CPU 사용률' `
    -Category 'perf' `
    -Cmd 'Get-Counter \Processor(_Total)\% Processor Time' `
    -Value @{ pct = $avg } `
    -Threshold $th `
    -Status $status
