param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# AP 서버 H/W LED는 육안 확인 항목. 점검자가 PWA에서 직접 체크.
$items = @(
    @{ id='ap.led.system'; label='시스템 LED' },
    @{ id='ap.led.psu';    label='Power Supply LED' },
    @{ id='ap.led.disk';   label='Disk LED' },
    @{ id='ap.cable';      label='케이블 연결상태' }
)
$out = foreach ($it in $items) {
    New-CheckResult `
        -Id $it.id `
        -Label $it.label `
        -Category 'visual' `
        -Method 'manual' `
        -Value $null `
        -Status 'pending_manual'
}
$out
