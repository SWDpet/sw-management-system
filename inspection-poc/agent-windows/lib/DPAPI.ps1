<#
.SYNOPSIS
  Windows DPAPI 매개 패스워드 암호화/복호화 helper.

.DESCRIPTION
  config 의 SSH/Telnet 패스워드를 plain text 로 저장하지 않기 위한 도구.
  Windows CurrentUser scope DPAPI 사용 — 동일 Windows 사용자 계정으로만 복호화 가능.
  다른 사용자 / 다른 PC 로 config 를 옮기면 복호화 실패 (의도된 동작 — NFR-1).

  PS 4.0 호환 (Windows Server 2012 R2 기본). 'ConvertFrom-SecureString -AsPlainText'
  (PS 7+) 미사용. Marshal.SecureStringToBSTR 로 plain 추출.

.EXAMPLE
  $enc = Protect-Password -Plain '1qkrdnrwls!'
  $dec = Unprotect-Password -Encrypted $enc   # → '1qkrdnrwls!'
#>

function Protect-Password {
    [CmdletBinding()]
    param([Parameter(Mandatory)][AllowEmptyString()][string] $Plain)
    if ([string]::IsNullOrEmpty($Plain)) {
        throw "Protect-Password: 빈 문자열은 암호화할 수 없습니다."
    }
    $sec = ConvertTo-SecureString -String $Plain -AsPlainText -Force
    return (ConvertFrom-SecureString -SecureString $sec)
}

function Unprotect-Password {
    [CmdletBinding()]
    param([Parameter(Mandatory)][string] $Encrypted)
    try {
        $sec = ConvertTo-SecureString -String $Encrypted -ErrorAction Stop
    } catch {
        throw ("Unprotect-Password: 복호화 실패. 이 config 는 다른 Windows 사용자 또는 다른 PC 의 DPAPI 키로 암호화되었을 수 있습니다. " +
               "(setup.ps1 을 재실행해서 본 PC + 본 사용자로 다시 저장하세요.) 원인: " + $_.Exception.Message)
    }
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($sec)
    try {
        return [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($bstr)
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
    }
}
