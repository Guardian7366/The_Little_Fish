param(
    [switch]$Test,
    [string]$OriginalJar = ''
)
$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$buildRoot = Join-Path $projectRoot 'build'
$classes = Join-Path $buildRoot 'classes'
$testClasses = Join-Path $buildRoot 'test-classes'
$resources = Join-Path $projectRoot 'src/main/resources'
$utf8 = New-Object System.Text.UTF8Encoding($false)

function Invoke-Checked([string]$Program, [string[]]$Arguments) {
    & $Program @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Program failed (exit $LASTEXITCODE)." }
}

function Reset-BuildFolder([string]$Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath($buildRoot).TrimEnd('\') + '\'
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing to clean outside build: $resolved"
    }
    if (Test-Path -LiteralPath $resolved) { Remove-Item -LiteralPath $resolved -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $resolved | Out-Null
}

Push-Location $projectRoot
try {
    $jar = (Get-Command jar -ErrorAction Stop).Source
    $javaBin = Split-Path $jar
    $javac = Join-Path $javaBin 'javac.exe'
    $java = Join-Path $javaBin 'java.exe'
    if (-not (Test-Path -LiteralPath $javac)) { throw 'Use a JDK 17 or newer with java, javac and jar in the same bin folder.' }
    Reset-BuildFolder $classes
    $sources = Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | Sort-Object FullName
    $sourceList = Join-Path $buildRoot 'main-sources.txt'
    [IO.File]::WriteAllLines($sourceList, [string[]]($sources | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }), $utf8)
    Write-Output 'Compiling game sources...'
    Invoke-Checked $javac @('-encoding', 'UTF-8', '--release', '17', '-d', $classes, ('@' + $sourceList))

    if ($Test) {
        Reset-BuildFolder $testClasses
        $testSources = Get-ChildItem 'src/test/java' -Recurse -Filter '*.java' | Sort-Object FullName
        $testList = Join-Path $buildRoot 'test-sources.txt'
        [IO.File]::WriteAllLines($testList, [string[]]($testSources | ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' }), $utf8)
        Invoke-Checked $javac @('-encoding', 'UTF-8', '--release', '17', '-cp', $classes, '-d', $testClasses, ('@' + $testList))
        $classpath = "$testClasses;$classes;$resources"
        foreach ($suite in @('CombatRegression', 'main.ResourceRegression', 'main.AudioRegression', 'main.RuntimeRegression', 'main.StoryRegression', 'main.FirstZoneRegression', 'main.TitleRegression', 'main.EnemyShotRegression', 'main.SecondZoneRegression', 'main.ThirdZoneRegression', 'main.FourthZoneRegression', 'main.SharkyRegression', 'main.FifthZoneRegression', 'main.FifthLightingRegression', 'main.FifthArtRegression', 'main.FishtellaRegression', 'main.SixthZoneRegression', 'main.TempleRegression', 'main.TempleExtensionRegression', 'main.ExtraRoomsRegression', 'main.LaterRoomsRegression')) {
            Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, $suite)
        }
        $saveArguments = @('-Djava.awt.headless=true', '-cp', $classpath, 'data.SaveRegression')
        if ($OriginalJar) { $saveArguments += [IO.Path]::GetFullPath($OriginalJar) }
        Invoke-Checked $java $saveArguments
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.FifthSaveRegression')
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.SixthSaveRegression')
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.TempleSaveRegression')
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.TempleExtensionSaveRegression')
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.ExtraRoomsSaveRegression')
        Invoke-Checked $java @('-Djava.awt.headless=true', '-cp', $classpath, 'data.LaterRoomsSaveRegression')
    }

    $destination = Join-Path $buildRoot 'TheLittleFish.jar'
    Invoke-Checked $jar @(('-J-Djava.io.tmpdir=' + $buildRoot), '--create', '--file', $destination, '--main-class', 'main.Main', '-C', $classes, '.', '-C', $resources, '.')
    Write-Output "Ready: $destination"
} finally { Pop-Location }
