param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# AP 서버 H/W LED는 육안 확인 항목. 점검자가 PWA에서 직접 체크.
# 케이블 연결상태는 inspect_template AP sort 6 (Adapter) 가 cover — ap-adapter.ps1 자동수집 + 점검자 보강.
$items = @(
    @{ id='ap.led.system'; label='시스템 LED' },
    @{ id='ap.led.psu';    label='Power Supply LED' },
    @{ id='ap.led.disk';   label='Disk LED' }
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
