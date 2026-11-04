$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot)
try {
    $fixtureRoot = Join-Path (Get-Location) 'build/base-ca4b3ad'
    New-Item -ItemType Directory -Force build | Out-Null
    git archive --format=zip --output=build/base-ca4b3ad.zip ca4b3ad src/main/java src/main/resources/maps/stage4.txt
    if ($LASTEXITCODE -ne 0) { throw 'Cannot read ca4b3ad sources' }
    Expand-Archive -LiteralPath build/base-ca4b3ad.zip -DestinationPath $fixtureRoot -Force
    $javaBin = Split-Path (Get-Command jar -ErrorAction Stop).Source
    $classes = Join-Path $fixtureRoot 'classes'
    New-Item -ItemType Directory -Force $classes | Out-Null
    $sources = @(Get-ChildItem (Join-Path $fixtureRoot 'src/main/java') -Recurse -Filter '*.java')
    $sources += Get-Item tools/fixtures/FifthBaseFixture.java
    $list = Join-Path $fixtureRoot 'sources.txt'
    [IO.File]::WriteAllLines($list, [string[]]($sources | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' }), [Text.UTF8Encoding]::new($false))
    & (Join-Path $javaBin 'javac.exe') -encoding UTF-8 --release 17 -d $classes ('@' + $list)
    if ($LASTEXITCODE -ne 0) { throw 'Baseline fixture compilation failed' }
    $classpath = "$classes;$(Join-Path $fixtureRoot 'src/main/resources');src/main/resources"
    & (Join-Path $javaBin 'java.exe') '-Djava.awt.headless=true' -cp $classpath FifthBaseFixture
    if ($LASTEXITCODE -ne 0) { throw 'Baseline save fixture failed' }
} finally { Pop-Location }
