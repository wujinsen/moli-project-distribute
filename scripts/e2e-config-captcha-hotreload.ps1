# E2E: captcha.enabled 运行期热生效（不重启）
# 用法: pwsh -File scripts/e2e-config-captcha-hotreload.ps1
$ErrorActionPreference = 'Stop'
$Base = 'http://localhost:8888'
$DisabledCode = 10005

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Uri,
        [hashtable]$Headers = @{},
        $Body = $null
    )
    $params = @{
        Method      = $Method
        Uri         = $Uri
        ContentType = 'application/json'
        Headers     = $Headers
        TimeoutSec  = 15
    }
    if ($null -ne $Body) {
        $params.Body = ($Body | ConvertTo-Json -Compress)
    }
    return Invoke-RestMethod @params
}

Write-Host '=== Step 0: login as admin ==='
$login = Invoke-Api -Method POST -Uri "$Base/login" -Body @{ userName = 'admin'; password = '123456' }
if ($login.code -ne 200 -or -not $login.data.token) {
    throw "login failed: code=$($login.code) msg=$($login.msg)"
}
$token = $login.data.token
$auth = @{ Authorization = $token }
Write-Host "token acquired: $($token.Substring(0, [Math]::Min(12, $token.Length)))..."

Write-Host '=== Step 1: captcha disabled by default (no DB override, yaml default false) ==='
$c1 = Invoke-Api -Method POST -Uri "$Base/captchaImage" -Headers $auth
if ($c1.code -ne $DisabledCode) {
    throw "expected captcha disabled code $DisabledCode, got $($c1.code) msg=$($c1.msg)"
}
Write-Host "OK disabled: code=$($c1.code) msg=$($c1.msg)"

Write-Host '=== Step 2: enable captcha via PUT /config (no restart) ==='
$put = Invoke-Api -Method PUT -Uri "$Base/config" -Headers $auth -Body @{
    configKey   = 'captcha.enabled'
    configValue = 'true'
}
if ($put.code -ne 200) {
    throw "PUT /config failed: code=$($put.code) msg=$($put.msg)"
}
Write-Host 'OK config override written'

Write-Host '=== Step 3: captcha should work immediately ==='
$c2 = Invoke-Api -Method POST -Uri "$Base/captchaImage" -Headers $auth
if ($c2.code -ne 200 -or -not $c2.data.uuid -or -not $c2.data.img) {
    throw "expected captcha enabled, got code=$($c2.code) msg=$($c2.msg)"
}
Write-Host "OK enabled: uuid=$($c2.data.uuid) imgLen=$($c2.data.img.Length)"

Write-Host '=== Step 4: reset to default via DELETE /config/{key} ==='
$del = Invoke-Api -Method DELETE -Uri "$Base/config/captcha.enabled" -Headers $auth
if ($del.code -ne 200) {
    throw "DELETE reset failed: code=$($del.code) msg=$($del.msg)"
}
Write-Host 'OK override removed'

Write-Host '=== Step 5: captcha disabled again without restart ==='
$c3 = Invoke-Api -Method POST -Uri "$Base/captchaImage" -Headers $auth
if ($c3.code -ne $DisabledCode) {
    throw "expected disabled again code $DisabledCode, got $($c3.code) msg=$($c3.msg)"
}
Write-Host "OK disabled again: code=$($c3.code)"

Write-Host '=== Step 6: verify GET /config/list shows source ==='
$list = Invoke-Api -Method GET -Uri "$Base/config/list" -Headers $auth
$captchaItem = $list.data | Where-Object { $_.configKey -eq 'captcha.enabled' }
if (-not $captchaItem) { throw 'captcha.enabled not in config list' }
if ($captchaItem.overridden) { throw 'expected overridden=false after reset' }
if ($captchaItem.effectiveValue -ne 'false') { throw "expected effective=false, got $($captchaItem.effectiveValue)" }
if ($captchaItem.source -notin @('DEFAULT', 'ENVIRONMENT')) {
    throw "expected source DEFAULT or ENVIRONMENT after reset, got $($captchaItem.source)"
}
Write-Host "OK list: effective=$($captchaItem.effectiveValue) source=$($captchaItem.source) overridden=$($captchaItem.overridden)"

Write-Host ''
Write-Host 'PASS: captcha.enabled hot-reload verified end-to-end (no restart)'
