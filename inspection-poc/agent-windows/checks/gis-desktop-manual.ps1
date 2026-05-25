param($Config)
. (Join-Path $PSScriptRoot '..\lib\Common.ps1')

# GIS Desktop Pro 데이터저장소 구동확인 — inspect_template GIS sort 3 (육안 점검).
# 점검자가 Desktop Pro 실행 후 GSS 데이터 로딩 확인 → UI 입력.
New-CheckResult `
    -Id 'gis.desktop.gss_store' `
    -Label 'Desktop Pro 데이터저장소 구동확인' `
    -Category 'visual' `
    -Method 'manual' `
    -Value $null `
    -Status 'pending_manual'
