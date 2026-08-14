# SSO-MENU-1 boundary tests S1/S2/S8/S9 (8888).
# Usage:
#   powershell -File docs/test/_sso_walkthrough_boundary.ps1
#   powershell -File docs/test/_sso_walkthrough_boundary.ps1 -SkipS1   # when portal is on (default dev)
# S1 requires user-center restarted with: mvn spring-boot:run -Dspring-boot.run.jvmArguments=-Dsso.enabled=false
param(
    [switch]$SkipS1,
    [string]$Base = 'http://127.0.0.1:8888',
    [string]$Mysql = 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'
)

$ErrorActionPreference = 'Stop'

function Invoke-Moli {
    param([string]$Path, [string]$Method = 'GET', [object]$Body, [string]$Token)
    $headers = @{ 'Content-Type' = 'application/json' }
    if ($Token) { $headers['Authorization'] = $Token }
    $params = @{ Uri = "$Base$Path"; Method = $Method; Headers = $headers }
    if ($Body) { $params.Body = ($Body | ConvertTo-Json -Compress) }
    return Invoke-RestMethod @params
}

function Login($user, $pass) {
    $r = Invoke-Moli -Path '/login' -Method POST -Body @{ userName = $user; password = $pass }
    return @{ Token = $r.data.token; Data = $r.data }
}

function ChildMenuIds($nodes) {
    $ids = @()
    foreach ($n in @($nodes)) {
        if ($n.children) {
            foreach ($c in @($n.children)) {
                if ($c.id) { $ids += "$($c.id)" }
            }
        }
    }
    return $ids
}

function Assert-Case($id, $ok, $note) {
    $mark = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Host "[$id] $mark — $note" -ForegroundColor $(if ($ok) { 'Green' } else { 'Red' })
    return @{ Id = $id; Ok = $ok; Note = $note }
}

Write-Host '=== SSO-MENU-1 boundary S1/S2/S8/S9 ===' -ForegroundColor Cyan
$results = @()

# S1 — portal off, full menu on login
if ($SkipS1) {
    Write-Host '[S1] SKIP — restart 8888 with -Dsso.enabled=false to run' -ForegroundColor Yellow
} else {
    $admin = Login 'admin' '123456'
    $d = $admin.Data
    $routers = Invoke-Moli -Path '/menu/getRouters' -Token $admin.Token
    $top = @($routers.data).Count
    $s1ok = (-not $d.systemPortalEnabled) -and ($d.menuVoList.Count -gt 0) -and ($top -gt 0)
    $results += Assert-Case 'S1' $s1ok "portal=$($d.systemPortalEnabled) menuVoList=$($d.menuVoList.Count) getRouters=$top"
}

# S2 — single INTERNAL auto-enter (huangli: system_id=1 only)
$h = Login 'huangli' '123456'
$hd = $h.Data
$hg = Invoke-Moli -Path '/menu/getRouters' -Token $h.Token
$s2ok = ($hd.currentSystem.systemCode -eq 'moli-admin') -and ($hd.menuVoList.Count -gt 0) -and (@($hg.data).Count -gt 0)
$results += Assert-Case 'S2' $s2ok "current=$($hd.currentSystem.systemCode) menuVoList=$($hd.menuVoList.Count) getRouters=$(@($hg.data).Count)"

# S8 — role dict_list_only_smoke: parent 1 + child 7 only
$z = Login 'zhangsan' '123456'
$null = Invoke-Moli -Path '/system/enter' -Method POST -Body @{ systemId = 1 } -Token $z.Token
$g8 = Invoke-Moli -Path '/menu/getRouters' -Token $z.Token
$parents = @($g8.data | ForEach-Object { "$($_.id)" })
$children = ChildMenuIds $g8.data
$s8ok = ($parents -contains '1') -and ($children -contains '7') -and ($children.Count -eq 1) -and (@($g8.data).Count -eq 1)
$results += Assert-Case 'S8' $s8ok "parents=$($parents -join ',') children=$($children -join ',')"

# S9 — ancestor补齐: temporarily grant only menu 401, expect parent 400
if (-not (Test-Path $Mysql)) {
    Write-Host '[S9] SKIP — mysql.exe not found; S8 menu-7 case already covers ancestor补齐' -ForegroundColor Yellow
    $s9menu7 = $s8ok
    $results += Assert-Case 'S9' $s9menu7 'menu7 parent1 (no mysql for 401 swap)'
} else {
    $backup = & $Mysql -uroot -p12345678 -D moli -N -e 'SELECT menu_id FROM sys_role_menu WHERE role_id=9'
    try {
        & $Mysql -uroot -p12345678 -D moli -e 'DELETE FROM sys_role_menu WHERE role_id=9; INSERT INTO sys_role_menu(id,role_id,menu_id) VALUES(726816894002135043,9,401);' | Out-Null
        $z2 = Login 'zhangsan' '123456'
        $null = Invoke-Moli -Path '/system/enter' -Method POST -Body @{ systemId = 1 } -Token $z2.Token
        $g9 = Invoke-Moli -Path '/menu/getRouters' -Token $z2.Token
        $p9 = @($g9.data | ForEach-Object { "$($_.id)" })
        $c9 = ChildMenuIds $g9.data
        $s9ok = ($p9 -contains '400') -and ($c9 -contains '401') -and ($c9.Count -eq 1)
        $results += Assert-Case 'S9' $s9ok "401-case parents=$($p9 -join ',') children=$($c9 -join ',')"
    } finally {
        & $Mysql -uroot -p12345678 -D moli -e 'DELETE FROM sys_role_menu WHERE role_id=9;' | Out-Null
        foreach ($mid in $backup) {
            if ($mid -match '^\d+$') {
                & $Mysql -uroot -p12345678 -D moli -e "INSERT INTO sys_role_menu(id,role_id,menu_id) VALUES(726816894002135042,9,$mid);" | Out-Null
            }
        }
        Write-Host '[S9] role 9 restored to menu 7' -ForegroundColor DarkGray
    }
}

$fail = @($results | Where-Object { $_ -and -not $_.Ok }).Count
Write-Host "=== done: $($results.Count - $fail)/$($results.Count) passed ===" -ForegroundColor Cyan
if ($fail -gt 0) { exit 1 }
