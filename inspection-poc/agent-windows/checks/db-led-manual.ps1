param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# DB 서버 (AIX) 육안점검 5 항목 — inspect_template DB sort 1~5.
# 점검자가 PWA / SW Manager UI 에서 직접 입력. agent 는 placeholder row 만 출력.
$items = @(
    @{ id='db.led.hw';     label='H/W LED 상태 점검' },
    @{ id='db.led.disk';   label='디스크 LED 상태 점검' },
    @{ id='db.led.fan';    label='FAN LED 상태 점검' },
    @{ id='db.led.power';  label='전원 LED 상태 점검' },
    @{ id='db.led.cable';  label='케이블 연결상태 점검' }
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
