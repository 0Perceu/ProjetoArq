param(
    [ValidateSet('portable', 'installer', 'all')]
    [string] $PackageType = 'portable'
)

$ErrorActionPreference = 'Stop'
if (-not $IsWindows -and $PSVersionTable.PSVersion.Major -ge 6) {
    throw 'Os pacotes Windows precisam ser gerados no Windows.'
}
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$moduleDir = Join-Path $projectRoot 'simumic'
$targetDir = Join-Path $moduleDir 'target'
$packageDir = Join-Path $targetDir 'package'
$distDir = Join-Path $projectRoot 'dist'
$wrapper = Join-Path $moduleDir 'mvnw.cmd'
$javaHome = $env:JAVA_HOME
if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\jpackage.exe'))) {
    throw 'Configure JAVA_HOME para um JDK 21 com jpackage.'
}
$javaRelease = Get-Content (Join-Path $javaHome 'release') -ErrorAction SilentlyContinue
if (($javaRelease -join ' ') -notmatch 'JAVA_VERSION="21[.]') {
    throw 'Este pacote requer JDK 21. Ajuste JAVA_HOME.'
}
if (-not (Test-Path $wrapper)) { throw 'Maven Wrapper não encontrado.' }

[xml] $pom = Get-Content (Join-Path $moduleDir 'pom.xml')
$version = [string] $pom.project.version
if ($version -notmatch '^[0-9]+\.[0-9]+\.[0-9]+$') { throw "Versão inválida no POM: $version" }
$parts = $version.Split('.')
foreach ($part in $parts) {
    if ([int64]::Parse($part) -gt 65535) { throw 'Cada componente da versão deve ser <= 65535.' }
}

function Invoke-Checked([string] $program, [string[]] $arguments) {
    & $program @arguments
    if ($LASTEXITCODE -ne 0) { throw "$program falhou com código $LASTEXITCODE." }
}

function Remove-Generated([string] $path, [string] $parent) {
    if (-not (Test-Path -LiteralPath $path)) { return }
    $parentFull = [IO.Path]::GetFullPath($parent).TrimEnd('\') + '\'
    $pathFull = [IO.Path]::GetFullPath($path)
    if (-not $pathFull.StartsWith($parentFull, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Recusa de limpeza fora de $parentFull"
    }
    Remove-Item -LiteralPath $pathFull -Recurse -Force
}

Push-Location $moduleDir
try {
    Invoke-Checked $wrapper @('-B', 'clean', 'verify', 'javafx:jlink')
} finally {
    Pop-Location
}
$runtime = Join-Path $targetDir 'runtime'
if (-not (Test-Path (Join-Path $runtime 'bin\java.exe'))) { throw 'jlink não criou target/runtime.' }
New-Item -ItemType Directory -Force -Path $packageDir, $distDir | Out-Null
$image = Join-Path $packageDir 'ProjetoArq'
Remove-Generated $image $packageDir
$jpackage = Join-Path $javaHome 'bin\jpackage.exe'
Invoke-Checked $jpackage @('--type', 'app-image', '--name', 'ProjetoArq', '--app-version', $version,
    '--runtime-image', $runtime, '--module', 'com.simumic/com.simumic.App', '--dest', $packageDir)
if (-not (Test-Path (Join-Path $image 'ProjetoArq.exe'))) { throw 'Launcher portátil não encontrado.' }

if ($PackageType -in @('portable', 'all')) {
    $zip = Join-Path $distDir "ProjetoArq-$version-windows-x64.zip"
    Remove-Generated $zip $distDir
    Compress-Archive -LiteralPath $image -DestinationPath $zip -CompressionLevel Optimal
    Write-Host "Portátil pronto: $zip"
}

if ($PackageType -in @('installer', 'all')) {
    $candle = Get-Command candle.exe -ErrorAction SilentlyContinue
    $light = Get-Command light.exe -ErrorAction SilentlyContinue
    if (-not $candle -or -not $light) {
        throw 'WiX Toolset 3.14.1 não encontrado. Instale-o para gerar o instalador; o pacote portátil continua disponível.'
    }
    $uuid = (Get-Content (Join-Path $projectRoot 'packaging\windows-upgrade-uuid.txt') -Raw).Trim()
    if ($uuid -notmatch '^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}$') {
        throw 'UUID de atualização inválido.'
    }
    $setup = Join-Path $distDir "ProjetoArq-$version-windows-x64-setup.exe"
    Remove-Generated $setup $distDir
    Invoke-Checked $jpackage @('--type', 'exe', '--name', 'ProjetoArq', '--app-image', $image,
        '--app-version', $version, '--win-per-user-install', '--win-shortcut', '--win-menu',
        '--win-menu-group', 'ProjetoArq', '--win-upgrade-uuid', $uuid,
        '--win-help-url', 'https://github.com/0Perceu/ProjetoArq', '--dest', $distDir)
    $produced = Join-Path $distDir "ProjetoArq-$version.exe"
    if (-not (Test-Path $produced)) { throw "Instalador não encontrado: $produced" }
    Move-Item -LiteralPath $produced -Destination $setup
    Write-Host "Instalador pronto: $setup"
}
