$ErrorActionPreference = 'Stop'
$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$moduleDir = Join-Path $projectRoot 'simumic'
$wrapper = Join-Path $moduleDir 'mvnw.cmd'
if (-not (Test-Path $wrapper)) { throw 'Maven Wrapper não encontrado.' }
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    throw 'Configure JAVA_HOME para um JDK 21 antes de iniciar.'
}
$release = Get-Content (Join-Path $env:JAVA_HOME 'release') -ErrorAction SilentlyContinue
if (($release -join ' ') -notmatch 'JAVA_VERSION="21[.]') {
    throw 'Este projeto requer JDK 21. Ajuste JAVA_HOME.'
}
Push-Location $moduleDir
try {
    & $wrapper 'javafx:run'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar o simulador.' }
} finally {
    Pop-Location
}
