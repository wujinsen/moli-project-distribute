$ErrorActionPreference = "Stop"

$singleRoot = "D:\work\moli_project\moli-project-single"
$distRoot = "D:\work\moli_project\moli-project-distribute"

$singleCommon = Join-Path $singleRoot "moli-common\src\main\java\com\moli\common"
$singleServer = Join-Path $singleRoot "moli-server\src\main\java\com\moli"

$distCommon = Join-Path $distRoot "moli-distribute-common\src\main\java\com\moli\common"
$ucCommon = Join-Path $distRoot "moli-user-center\moli-user-center-common\src\main\java\com\moli\user\center\common"
$ucServer = Join-Path $distRoot "moli-user-center\moli-user-center-server\src\main\java\com\moli\user\center\server"

function Transform-UcCommon([string]$content) {
    $content = $content -replace 'package com\.moli\.common\.domain', 'package com.moli.user.center.common.domain'
    $content = $content -replace 'import com\.moli\.common\.domain\.entity', 'import com.moli.user.center.common.domain.entity'
    $content = $content -replace 'import com\.moli\.common\.domain\.vo', 'import com.moli.user.center.common.domain.vo'
    $content = $content -replace 'import com\.moli\.common\.domain\.dto', 'import com.moli.user.center.common.domain.dto'
    return $content
}

function Transform-Server([string]$content) {
    $content = $content -replace 'package com\.moli\.system\.', 'package com.moli.user.center.server.'
    $content = $content -replace 'package com\.moli\.operation\.', 'package com.moli.user.center.server.operation.'
    $content = $content -replace 'package com\.moli\.config', 'package com.moli.user.center.server.config'
    $content = $content -replace 'package com\.moli\.aspectj', 'package com.moli.user.center.server.aspectj'
    $content = $content -replace 'import com\.moli\.system\.', 'import com.moli.user.center.server.'
    $content = $content -replace 'import com\.moli\.operation\.', 'import com.moli.user.center.server.operation.'
    $content = $content -replace 'import com\.moli\.config\.', 'import com.moli.user.center.server.config.'
    $content = $content -replace 'import com\.moli\.common\.domain\.entity', 'import com.moli.user.center.common.domain.entity'
    $content = $content -replace 'import com\.moli\.common\.domain\.vo', 'import com.moli.user.center.common.domain.vo'
    $content = $content -replace 'import com\.moli\.common\.domain\.dto', 'import com.moli.user.center.common.domain.dto'
    return $content
}

function Copy-JavaFile($src, $dest, $transform) {
    $dir = Split-Path $dest -Parent
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir | Out-Null }
    $content = Get-Content $src -Raw -Encoding UTF8
    if ($transform) { $content = & $transform $content }
    [System.IO.File]::WriteAllText($dest, $content, [System.Text.UTF8Encoding]::new($false))
}

# 1. Sync shared moli-distribute-common (overwrite + add)
$sharedRelative = @(
    "core\MoliResult.java", "core\BaseEntity.java", "core\BasePage.java", "core\IdGenerator.java", "core\SnowflakeIdWorker.java",
    "constant\CommonConstant.java", "constant\Constants.java", "constant\RedisConstant.java",
    "constant\PermissionConstants.java", "constant\SystemConstant.java", "constant\SystemGroupConstant.java", "constant\ShiroSessionConstant.java",
    "enums\ResponseCodeEnums.java", "enums\BusinessTypeEnum.java", "enums\OperatorTypeEnum.java", "enums\HttpMethodEnum.java",
    "exception\BaseException.java", "exception\GlobalExceptionHandler.java",
    "page\PageReq.java", "page\PageRes.java",
    "log\MoliLog.java",
    "minio\MinioProperty.java",
    "utils\MoliDateUtils.java", "utils\Base64.java", "utils\SpringUtil.java", "utils\ServletUtils.java",
    "utils\I18nUtils.java", "utils\MenuRouteNameUtils.java",
    "utils\IpUtils.java", "utils\UserAgentUtils.java", "utils\HTMLFilter.java", "utils\EscapeUtil.java",
    "utils\string\StringUtils.java", "utils\string\StrFormatter.java", "utils\string\Convert.java", "utils\string\CharsetKit.java"
)
foreach ($rel in $sharedRelative) {
    $src = Join-Path $singleCommon $rel
    $dest = Join-Path $distCommon $rel
    if (Test-Path $src) { Copy-JavaFile $src $dest $null; Write-Host "common: $rel" }
}

# 2. Sync user-center-common domain (entity, vo, dto)
Get-ChildItem (Join-Path $singleCommon "domain") -Recurse -Filter "*.java" | ForEach-Object {
    $rel = $_.FullName.Substring((Join-Path $singleCommon "domain").Length + 1)
    $dest = Join-Path (Join-Path $ucCommon "domain") $rel
    Copy-JavaFile $_.FullName $dest { param($c) Transform-UcCommon $c }
    Write-Host "uc-common: domain\$rel"
}

# Remove obsolete CommonPermissionConstant if PermissionConstants replaces it
$oldPerm = Join-Path $ucCommon "constant\CommonPermissionConstant.java"
if (Test-Path $oldPerm) { Remove-Item $oldPerm -Force; Write-Host "removed obsolete CommonPermissionConstant.java" }

# 3. Backup microservice-specific files
$keepServerFiles = @(
    "UserCenterApplication.java",
    "provider\UserServerProvider.java"
)
$backupDir = Join-Path $env:TEMP "moli-uc-migration-backup"
if (Test-Path $backupDir) { Remove-Item $backupDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
foreach ($rel in $keepServerFiles) {
    $src = Join-Path $ucServer $rel
    if (Test-Path $src) {
        $destBackup = Join-Path $backupDir $rel
        $destBackupDir = Split-Path $destBackup -Parent
        if (-not (Test-Path $destBackupDir)) { New-Item -ItemType Directory -Force -Path $destBackupDir | Out-Null }
        Copy-Item $src $destBackup -Force
    }
}

# 4. Remove old server business packages (keep UserCenterApplication + provider)
$removeDirs = @("controller", "service", "mapper", "config", "operation", "aspectj")
foreach ($d in $removeDirs) {
    $path = Join-Path $ucServer $d
    if (Test-Path $path) { Remove-Item $path -Recurse -Force; Write-Host "removed old $d" }
}

# 5. Copy server: system/*
$systemSrc = Join-Path $singleServer "system"
Get-ChildItem $systemSrc -Recurse -Filter "*.java" | ForEach-Object {
    $rel = $_.FullName.Substring($systemSrc.Length + 1)
    $dest = Join-Path $ucServer $rel
    Copy-JavaFile $_.FullName $dest { param($c) Transform-Server $c }
    Write-Host "server: $rel"
}

# 6. Copy server: operation/*
$opSrc = Join-Path $singleServer "operation"
if (Test-Path $opSrc) {
    Get-ChildItem $opSrc -Recurse -Filter "*.java" | ForEach-Object {
        $rel = $_.FullName.Substring($opSrc.Length + 1)
        $dest = Join-Path (Join-Path $ucServer "operation") $rel
        Copy-JavaFile $_.FullName $dest { param($c) Transform-Server $c }
        Write-Host "operation: $rel"
    }
}

# 7. Copy server: config/* and aspectj/*
foreach ($pkg in @("config", "aspectj")) {
    $pkgSrc = Join-Path $singleServer $pkg
    if (Test-Path $pkgSrc) {
        Get-ChildItem $pkgSrc -Recurse -Filter "*.java" | ForEach-Object {
            $rel = $_.FullName.Substring($pkgSrc.Length + 1)
            $dest = Join-Path (Join-Path $ucServer $pkg) $rel
            Copy-JavaFile $_.FullName $dest { param($c) Transform-Server $c }
            Write-Host "${pkg}: $rel"
        }
    }
}

# 8. Restore microservice-specific files
foreach ($rel in $keepServerFiles) {
    $src = Join-Path $backupDir $rel
    $dest = Join-Path $ucServer $rel
    if (Test-Path $src) { Copy-Item $src $dest -Force; Write-Host "restored $rel" }
}

Write-Host "Migration script completed."
