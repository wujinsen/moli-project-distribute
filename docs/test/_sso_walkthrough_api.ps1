# SSO-MENU-1 API smoke (8888). Usage: powershell -File docs/test/_sso_walkthrough_api.ps1
$ErrorActionPreference = 'Stop'
$base = 'http://127.0.0.1:8888'

function Invoke-Moli {
    param([string]$Path, [string]$Method = 'GET', [object]$Body, [string]$Token)
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($Token) { $headers['Authorization'] = $Token }
    $params = @{ Uri = "$base$Path"; Method = $Method; Headers = $headers }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-RestMethod @params
}

function TopMenuNames($data) {
    if (-not $data) { return @() }
    return @($data | ForEach-Object { if ($_.meta.title) { $_.meta.title } elseif ($_.name) { $_.name } else { $_.path } })
}

function Login($user, $pass) {
    $r = Invoke-Moli -Path '/login' -Method POST -Body @{ userName = $user; password = $pass }
    return @{ Token = $r.data.token; Result = $r }
}

Write-Host "=== SSO-MENU-1 API smoke ===" -ForegroundColor Cyan

# S10 / F-SSO-2: admin login, no enter -> empty getRouters
$admin = Login 'admin' '123456'
$a = $admin.Result.data
Write-Host "[S10] login portal=$($a.systemPortalEnabled) menuVoList=$($a.menuVoList.Count) current=$($a.currentSystem.systemCode)"
$g0 = Invoke-Moli -Path '/menu/getRouters' -Token $admin.Token
$names0 = TopMenuNames $g0.data
Write-Host "[S10] getRouters count=$($names0.Count) -> $(if ($names0.Count -eq 0) { 'PASS' } else { 'FAIL: ' + ($names0 -join ',') })"

# S3: enter moli-admin (id=1)
$enter1 = Invoke-Moli -Path '/system/enter' -Method POST -Body @{ systemId = 1 } -Token $admin.Token
$e1 = $enter1.data
Write-Host "[S3] enter admin menuVoList=$($e1.menuVoList.Count) system=$($e1.currentSystem.systemCode)"
$g1 = Invoke-Moli -Path '/menu/getRouters' -Token $admin.Token
$names1 = TopMenuNames $g1.data
$hasOps = ($names1 -match '运营') -or ($g1.data.path -contains 'operation')
$has500 = ($names1 -match 'ChatGPT|500') -or ($g1.data | Where-Object { $_.name -eq 'ChatGPT' })
$has600 = ($names1 -match '烛龙|BI|600') -or ($g1.data | Where-Object { $_.name -match 'bi|BI' })
$has900 = ($names1 -match '知识库|900') -or ($g1.data | Where-Object { $_.name -match 'Knowledge|knowledge' })
Write-Host "[S3] getRouters top=$($names1 -join ' | ')"
Write-Host "[S3] no 500/600: $(if (-not $has500 -and -not $has600) { 'PASS' } else { 'FAIL' }); has KB hint: $has900"

# S5: enter EXTERNAL moli-knowledge (39)
$enter39 = Invoke-Moli -Path '/system/enter' -Method POST -Body @{ systemId = 39 } -Token $admin.Token
$e39 = $enter39.data
Write-Host "[S5] enter 39 redirect=$($e39.redirectUrl) menus=$($e39.menuVoList.Count)"
$g39 = Invoke-Moli -Path '/menu/getRouters' -Token $admin.Token
Write-Host "[S5] getRouters after EXTERNAL count=$(@($g39.data).Count) -> $(if (@($g39.data).Count -eq 0) { 'PASS' } else { 'FAIL' })"

# S4: switch back to admin
$sw = Invoke-Moli -Path '/system/switch' -Method POST -Body @{ systemId = 1 } -Token $admin.Token
$g2 = Invoke-Moli -Path '/menu/getRouters' -Token $admin.Token
$names2 = TopMenuNames $g2.data
Write-Host "[S4] switch->admin getRouters count=$($names2.Count)"

# S7: getMenuTreeAll (superadmin full tree)
$tree = Invoke-Moli -Path '/menu/getMenuTreeAll' -Token $admin.Token
$treeCount = @($tree.data).Count
Write-Host "[S7] getMenuTreeAll count=$treeCount -> $(if ($treeCount -gt 0) { 'PASS' } else { 'FAIL' })"

Write-Host "=== done ===" -ForegroundColor Cyan
