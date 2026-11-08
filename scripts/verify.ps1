#Requires -Version 7.0
<#
.SYNOPSIS
Builds Nows, inspects the artifacts and starts every Minecraft adapter on the real client with a consumer mod.

.DESCRIPTION
One gate for "the loader starts and an outside mod can use the API". Per adapter it checks:
  1. build and check pass (Gradle);
  2. artifacts: policy and mixin configs, service files, no doubled or unremapped classes, jars newer than sources;
  3. the consumer fixture (fixtures/consumer) builds through the Gradle plugin and its jar is well formed;
  4. the real client starts, reaches the main menu, and the fixture reports a result for every API it covers;
  5. the full log has no failure pattern, except the narrow client-launch allowlist below;
  6. startup time against a baseline, and the same API results on every adapter.
It exits non-zero on any failure, a missing summary line, a hang, a failure-pattern hit or a stale jar.

Needs network access for Mojang's launcher metadata, libraries and assets (cached under .nows/smoke) and a display
for the client window. Launching is implemented for Windows x64.

.EXAMPLE
pwsh scripts/verify.ps1 -JdkRoots G:\Java

.EXAMPLE
pwsh scripts/verify.ps1 -Versions 1.20.1,26.2 -SkipGradle -BaselineFile baseline.json
#>
[CmdletBinding()]
param(
    [string[]] $Versions,
    [string] $RepoRoot = (Split-Path $PSScriptRoot -Parent),
    # JDK used to run Gradle. The build compiles with a Java 25 toolchain, so this must be Java 25 or newer.
    [string] $GradleJdk = $env:JAVA_HOME,
    # Folders that contain JDK installs. Each adapter is launched with the Java version Mojang requires (minimum 17).
    [string[]] $JdkRoots = @(),
    [int] $LaunchTimeoutSec = 300,
    # JSON map of adapter to seconds until the main menu, from an earlier run. Fails when startup is much slower.
    [string] $BaselineFile,
    [string] $WriteBaselineFile,
    [double] $MaxSlowdown = 1.5,
    [int] $SlowdownSlackSec = 5,
    # Start the game without the consumer fixture. Used to measure a baseline on a ref that has no fixture.
    [switch] $NoMod,
    [switch] $SkipGradle
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$failures = [System.Collections.Generic.List[string]]::new()
function Fail([string] $scope, [string] $message) { $failures.Add("[$scope] $message"); Write-Host "FAIL [$scope] $message" -ForegroundColor Red }
function Pass([string] $scope, [string] $message) { Write-Host "ok   [$scope] $message" }

# `pwsh -File` passes "-Versions a,b" as one string, so split list parameters here.
$Versions = @($Versions | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$JdkRoots = @($JdkRoots | ForEach-Object { $_ -split ',' } | Where-Object { $_ })
$RepoRoot = (Resolve-Path $RepoRoot).Path
$WorkDir = Join-Path $RepoRoot '.nows\verify'
$Cache = Join-Path $RepoRoot '.nows\smoke'
New-Item -ItemType Directory -Force $WorkDir, $Cache | Out-Null

if (-not $Versions) {
    $Versions = Select-String -Path (Join-Path $RepoRoot 'settings.gradle.kts') -Pattern '"mc:([^"]+)"' |
        ForEach-Object { $_.Matches[0].Groups[1].Value }
}
$nowsVersion = (Select-String -Path (Join-Path $RepoRoot 'gradle.properties') -Pattern '^nows_version=(.+)$').Matches[0].Groups[1].Value.Trim()

# ---- JDKs ---------------------------------------------------------------------------------------------------------
$jdks = @{}
$candidates = @($GradleJdk) + @($JdkRoots | ForEach-Object { Get-ChildItem $_ -Directory | ForEach-Object FullName })
foreach ($dir in $candidates | Where-Object { $_ }) {
    $release = Join-Path $dir 'release'
    if ((Test-Path $release) -and ((Get-Content $release -Raw) -match 'JAVA_VERSION="(\d+)')) {
        $jdks[[int]$Matches[1]] = $dir
    }
}
if (-not $GradleJdk -or -not $jdks.ContainsKey(25)) {
    Write-Host 'BLOCKED: Gradle needs a Java 25 JDK. Pass -GradleJdk <path> or set JAVA_HOME.' -ForegroundColor Yellow
    exit 2
}
$toolchainPaths = ($jdks.Values | Select-Object -Unique) -join ','

# ---- 1. Gradle ----------------------------------------------------------------------------------------------------
$gradlew = Join-Path $RepoRoot ($IsWindows ? 'gradlew.bat' : 'gradlew')
function Invoke-Gradle([string[]] $arguments) {
    $env:JAVA_HOME = $GradleJdk
    & $gradlew --console=plain "-Porg.gradle.java.installations.paths=$toolchainPaths" @arguments 2>&1
}
if (-not $SkipGradle) {
    $output = Invoke-Gradle @('-p', $RepoRoot, 'build', 'check')
    if ($LASTEXITCODE -ne 0) {
        Fail 'gradle' "build check failed (exit $LASTEXITCODE)"
        $output | Select-Object -Last 40 | ForEach-Object { Write-Host $_ }
        exit 1
    }
    Pass 'gradle' 'build and check passed'
}

# Gradle init script that prints the runtime classpath the way the installer lays it out.
$initScript = Join-Path $WorkDir 'classpath.init.gradle'
Set-Content $initScript @'
allprojects {
  if (path == ':runtime') {
    afterEvaluate {
      def libs = configurations.create('nowsLaunchLibs')
      ['com.squareup.moshi:moshi:' + moshi_version, 'com.squareup.okio:okio-jvm:' + okio_version,
       'org.jetbrains.kotlin:kotlin-stdlib:' + kotlin_version, 'org.jetbrains.kotlin:kotlin-stdlib-jdk7:' + kotlin_version,
       'org.jetbrains.kotlin:kotlin-stdlib-jdk8:' + kotlin_version,
       'org.jetbrains:annotations:' + jetbrains_annotations_version].each {
        dependencies.add('nowsLaunchLibs', it) { transitive = false }
      }
      tasks.register('printNowsLaunchClasspath') {
        dependsOn tasks.jar
        doLast {
          def files = configurations.runtimeClasspath.files + libs.files + [tasks.jar.archiveFile.get().asFile]
          new File(System.getProperty('nows.cpOut')).text = files*.absolutePath.join('\n')
        }
      }
    }
  }
}
'@
$cpFile = Join-Path $WorkDir 'nows-classpath.txt'
Remove-Item $cpFile -ErrorAction SilentlyContinue
$output = Invoke-Gradle @('-q', '-p', $RepoRoot, '-I', $initScript, ':runtime:printNowsLaunchClasspath', "-Dnows.cpOut=$cpFile")
if ($LASTEXITCODE -ne 0 -or -not (Test-Path $cpFile)) { $output | Select-Object -Last 30 | ForEach-Object { Write-Host $_ }; Fail 'gradle' 'could not resolve the runtime classpath'; exit 1 }
# Minecraft owns these. Nows does not ship them, so they must not shadow the game's copies.
$nowsClasspath = Get-Content $cpFile | Where-Object { $_ -and $_ -notmatch '[\\/](gson|jspecify|processor|javapoet|error_prone|jakarta)[^\\/]*\.jar$' }

# ---- 2. Artifact checks -------------------------------------------------------------------------------------------
function Get-JarEntries([string] $jar) {
    $zip = [IO.Compression.ZipFile]::OpenRead($jar)
    try { return @($zip.Entries | ForEach-Object FullName) } finally { $zip.Dispose() }
}
function Read-JarText([string] $jar, [string] $entry) {
    $zip = [IO.Compression.ZipFile]::OpenRead($jar)
    try {
        $e = $zip.GetEntry($entry)
        if (-not $e) { return $null }
        $reader = [IO.StreamReader]::new($e.Open())
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    } finally { $zip.Dispose() }
}
function Get-BuiltJar([string] $moduleDir, [string] $prefix) {
    Get-ChildItem (Join-Path $RepoRoot "$moduleDir\build\libs") -Filter "$prefix-*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1
}
function Get-NewestSource([string[]] $dirs) {
    $newest = [datetime]::MinValue
    foreach ($dir in $dirs | Where-Object { Test-Path $_ }) {
        $latest = Get-ChildItem $dir -Recurse -File | Sort-Object LastWriteTime -Descending | Select-Object -First 1
        if ($latest -and $latest.LastWriteTime -gt $newest) { $newest = $latest.LastWriteTime }
    }
    $newest
}

$nowsModules = @(
    @{ Dir = 'core'; Prefix = 'nows-core' }, @{ Dir = 'minecraft'; Prefix = 'nows-minecraft' },
    @{ Dir = 'runtime'; Prefix = 'nows-runtime' }, @{ Dir = 'integrations\kdl'; Prefix = 'nows-integration-kdl' },
    @{ Dir = 'integrations\geb'; Prefix = 'nows-integration-geb' }, @{ Dir = 'integrations\logging'; Prefix = 'nows-integration-logging' },
    @{ Dir = 'integrations\network'; Prefix = 'nows-integration-network' }, @{ Dir = 'integrations\mixin'; Prefix = 'nows-integration-mixin' })

function Test-Artifacts([string] $version) {
    $scope = "artifact $version"
    $adapterJar = Get-BuiltJar "mc\$version" "nows-mc-$version"
    if (-not $adapterJar) { Fail $scope "adapter jar is missing under mc\$version\build\libs"; return $null }
    $moduleJars = @($nowsModules | ForEach-Object { Get-BuiltJar $_.Dir $_.Prefix })
    if ($moduleJars.Count -ne $nowsModules.Count) {
        $present = $moduleJars | ForEach-Object { $_.Name }
        Fail $scope "a Nows module jar is missing, found only: $($present -join ', ')"
        return $null
    }
    $jars = @($adapterJar) + $moduleJars

    # Jars must be newer than the sources they were built from.
    foreach ($module in $nowsModules) {
        $jar = Get-BuiltJar $module.Dir $module.Prefix
        if ($jar.LastWriteTime -lt (Get-NewestSource @(Join-Path $RepoRoot "$($module.Dir)\src")).AddSeconds(-2)) { Fail $scope "$($jar.Name) is older than its sources (stale jar)" }
    }
    $newestAdapter = Get-NewestSource @((Join-Path $RepoRoot "mc\$version\src"), (Join-Path $RepoRoot 'mc\shared-main\src'))
    if ($adapterJar.LastWriteTime -lt $newestAdapter.AddSeconds(-2)) { Fail $scope "$($adapterJar.Name) is older than its sources (stale jar)" }

    # Policy and mixin configs.
    $minecraftJar = Get-BuiltJar 'minecraft' 'nows-minecraft'
    $policy = Read-JarText $minecraftJar.FullName "META-INF/nows/mc/$version/nows-minecraft.properties"
    $adapterEntries = Get-JarEntries $adapterJar.FullName
    if (-not $policy) { Fail $scope "policy META-INF/nows/mc/$version/nows-minecraft.properties is missing from the minecraft jar" }
    elseif ($policy -match '(?m)^runtime\.builtinMixinConfigs=(.*)$') {
        foreach ($config in $Matches[1].Split(',', [StringSplitOptions]::RemoveEmptyEntries) | ForEach-Object Trim) {
            $json = Read-JarText $adapterJar.FullName $config
            if (-not $json) { Fail $scope "mixin config $config named by the policy is missing from the adapter jar"; continue }
            $parsed = $json | ConvertFrom-Json
            $declared = @(foreach ($key in 'mixins', 'client', 'server') { if ($parsed.PSObject.Properties[$key]) { $parsed.$key } }) | Where-Object { $_ }
            foreach ($mixin in $declared) {
                $entry = ($parsed.package + '.' + $mixin).Replace('.', '/') + '.class'
                if ($adapterEntries -notcontains $entry) { Fail $scope "mixin class $entry listed in $config does not exist" }
            }
            Pass $scope "mixin config $config lists $(@($declared).Count) existing mixin classes"
        }
    }
    foreach ($required in 'space/nows/mc/internal/MinecraftIntegration.class', 'space/nows/mc/internal/ClientHooks.class') {
        if ($adapterEntries -notcontains $required) { Fail $scope "$required is missing from the adapter jar" }
    }

    # Service files must name classes that exist in the same jar. Nows classes must live in exactly one jar.
    $owners = @{}
    foreach ($jar in $jars) {
        $entries = Get-JarEntries $jar.FullName
        foreach ($service in $entries | Where-Object { $_ -like 'META-INF/services/*' -and -not $_.EndsWith('/') }) {
            foreach ($line in (Read-JarText $jar.FullName $service) -split "`r?`n" | ForEach-Object { ($_ -replace '#.*$', '').Trim() } | Where-Object { $_ }) {
                if ($entries -notcontains ($line.Replace('.', '/') + '.class')) { Fail $scope "$($jar.Name): service $service names missing class $line" }
            }
        }
        foreach ($entry in $entries | Where-Object { $_ -like 'space/nows/*.class' }) {
            if ($owners.ContainsKey($entry) -and $owners[$entry] -ne $jar.Name) { Fail $scope "$entry is in both $($owners[$entry]) and $($jar.Name)" }
            $owners[$entry] = $jar.Name
        }
    }

    # No intermediary or SRG names: the game runs with Mojang names.
    $zip = [IO.Compression.ZipFile]::OpenRead($adapterJar.FullName)
    try {
        $bad = 0
        foreach ($entry in $zip.Entries | Where-Object { $_.FullName.EndsWith('.class') }) {
            $stream = $entry.Open(); $buffer = [IO.MemoryStream]::new(); $stream.CopyTo($buffer); $stream.Dispose()
            $text = [Text.Encoding]::Latin1.GetString($buffer.ToArray())
            if ($text -match 'net/minecraft/class_\d+|\b(field|method)_\d{4,}\b|\b(func|field)_\d+_[A-Za-z_]+') { Fail $scope "$($entry.FullName) references intermediary or SRG names"; $bad++ }
        }
        if ($bad -eq 0) { Pass $scope "no unremapped names in $($adapterJar.Name)" }
    } finally { $zip.Dispose() }
    return $adapterJar
}

function Test-FixtureJar([string] $version, [string] $jarPath) {
    $scope = "fixture $version"
    $entries = Get-JarEntries $jarPath
    $kdl = Read-JarText $jarPath 'nows.mod.kdl'
    if (-not $kdl) { Fail $scope 'nows.mod.kdl is missing'; return }
    if ($kdl -match '\$\{') { Fail $scope 'nows.mod.kdl still has unexpanded ${...} placeholders' }
    if ($kdl -notmatch "minecraft=`"$([regex]::Escape($version))`"") { Fail $scope "nows.mod.kdl does not target Minecraft $version" }
    foreach ($class in [regex]::Matches($kdl, '(?:entrypoint|listener)\s+"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }) {
        if ($entries -notcontains ($class.Replace('.', '/') + '.class')) { Fail $scope "$class named in nows.mod.kdl is not in the jar" }
    }
    if (@($entries | Where-Object { $_ -like 'space/nows/mc/*' -or $_ -like 'space/nows/platform/*' }).Count -gt 0) { Fail $scope 'the fixture bundles Nows classes, it must compile against them only' }
    Pass $scope 'metadata expanded, entrypoint and listener classes present, no bundled Nows classes'
}

# ---- 3. Launch ----------------------------------------------------------------------------------------------------
$FailurePattern = 'Exception|Error|FATAL|NoClassDefFoundError|ClassNotFoundException|NoSuchMethodError|NoSuchFieldError|AbstractMethodError|LinkageError|IncompatibleClassChangeError|UnsupportedClassVersionError|Mixin apply failed|InvalidMixinException|MixinTransformerError|mixin target not found|refmap|missing or incompatible|failed to load|could not load|was not found|unknown registry|duplicate registration|ServiceConfigurationError|crash report'

# Log entries that every client launch with the dummy token '--accessToken 0' produces. They come from Mojang's online
# services code, not from Nows, and were reproduced on unmodified main (see the baseline run in the pull request).
# An entry is allowed only when its first line matches exactly and every other line that matches the failure pattern
# is one of the listed exception lines or a stack frame inside com.mojang.authlib or com.mojang.realmsclient.
$ExpectedClientLaunchEntries = @(
    @{ Level = 'ERROR'; Message = '^Failed to verify authentication$' },
    @{ Level = 'ERROR'; Message = '^Failed to fetch user properties$' },
    @{ Level = 'ERROR'; Message = '^Failed to retrieve profile key pair$' },
    @{ Level = 'ERROR'; Message = '^Failed to fetch Realms feature flags$' },
    @{ Level = 'ERROR'; Message = '^Couldn''t connect to realms$' },
    @{ Level = 'INFO'; Message = '^Could not authorize you against Realms server: java\.lang\.RuntimeException: Failed to parse into SignedJWT: 0$' })
$ExpectedExceptionLines = @(
    '^com\.mojang\.authlib\.exceptions\.InvalidCredentialsException: Status: 401$',
    '^Caused by: com\.mojang\.authlib\.exceptions\.MinecraftClientHttpException: Status: 401$',
    '^Caused by: MinecraftClientHttpException\[type=HTTP_ERROR, status=401, response=ErrorResponse\[path=/?[a-z/]+, [^\]]*\]\]$',
    '^java\.io\.IOException: Could not retrieve profile key pair$',
    '^Caused by: java\.lang\.RuntimeException: Failed to parse into SignedJWT: 0$',
    '^com\.mojang\.realmsclient\.exception\.RealmsServiceException: Realms authentication error with message ''java\.lang\.RuntimeException: Failed to parse into SignedJWT: 0''$',
    '^(Caused by: )?com\.mojang\.authlib\.exceptions\.[A-Za-z]+Exception: .*$')

function Get-LogEntries([string] $text) {
    $entries = [System.Collections.Generic.List[object]]::new()
    $current = $null
    foreach ($line in $text -split "`r?`n") {
        if ($line -match '^\[\d{2}:\d{2}:\d{2}\] \[[^\]/]*/(?<level>[A-Z]+)\]: (?<message>.*)$') {
            $current = @{ Level = $Matches.level; Message = $Matches.message; Lines = [System.Collections.Generic.List[string]]::new() }
            $entries.Add($current)
        } elseif ($current -and $line.Trim()) {
            $current.Lines.Add($line.Trim())
        } elseif (-not $current -and $line.Trim()) {
            $entries.Add(@{ Level = 'RAW'; Message = $line.Trim(); Lines = [System.Collections.Generic.List[string]]::new() })
        }
    }
    $entries
}

function Test-LogEntry($entry) {
    $hitLines = @($entry.Lines | Where-Object { $_ -match $FailurePattern })
    if ($entry.Message -notmatch $FailurePattern -and $hitLines.Count -eq 0) { return $true }
    $allowed = $ExpectedClientLaunchEntries | Where-Object { $_.Level -eq $entry.Level -and $entry.Message -match $_.Message }
    if (-not $allowed) { return $false }
    foreach ($line in $hitLines) {
        $expected = ($ExpectedExceptionLines | Where-Object { $line -match $_ }) -or ($line -match '^at com\.mojang\.(authlib|realmsclient)\.')
        if (-not $expected) { return $false }
    }
    return $true
}

function Get-MojangMetadata([string] $version) {
    $manifest = Invoke-RestMethod 'https://piston-meta.mojang.com/mc/game/version_manifest_v2.json'
    $entry = $manifest.versions | Where-Object id -eq $version
    if (-not $entry) { throw "Minecraft $version is not in Mojang's version manifest" }
    Invoke-RestMethod $entry.url
}
function Get-VerifiedFile([string] $url, [string] $destination, [string] $sha1) {
    if ((Test-Path $destination) -and (-not $sha1 -or (Get-FileHash $destination -Algorithm SHA1).Hash -eq $sha1)) { return }
    New-Item -ItemType Directory -Force (Split-Path $destination) | Out-Null
    Invoke-WebRequest $url -OutFile $destination -UseBasicParsing
    if ($sha1 -and (Get-FileHash $destination -Algorithm SHA1).Hash -ne $sha1) { throw "SHA1 mismatch for $url" }
}
function Test-LibraryRules($rules) {
    if (-not $rules) { return $true }
    $allowed = $false
    foreach ($rule in $rules) {
        $applies = $true
        if ($rule.PSObject.Properties['os'] -and $rule.os) {
            if ($rule.os.PSObject.Properties['name'] -and $rule.os.name -ne 'windows') { $applies = $false }
            if ($rule.os.PSObject.Properties['arch'] -and $rule.os.arch -ne 'x86_64') { $applies = $false }
        }
        if ($rule.PSObject.Properties['features']) { $applies = $false }
        if ($applies) { $allowed = ($rule.action -eq 'allow') }
    }
    $allowed
}

$MenuMarkers = @('Invoking Minecraft main class', 'Mixin applied: .*TitleScreenMixin', 'Sound engine started')

function Start-Adapter([string] $version, [string] $modJar) {
    $scope = "launch $version"
    if (-not $IsWindows) { Fail $scope 'launching is implemented for Windows x64 only'; return $null }
    $meta = Get-MojangMetadata $version
    $need = [Math]::Max(17, [int]$meta.javaVersion.majorVersion)   # the Nows runtime is compiled for Java 17
    if (-not $jdks.ContainsKey($need)) { Fail $scope "needs a Java $need JDK in -JdkRoots"; return $null }
    $java = Join-Path $jdks[$need] ($IsWindows ? 'bin\java.exe' : 'bin/java')

    $runDir = Join-Path $Cache "run\$version"
    $natives = Join-Path $runDir 'natives'; $game = Join-Path $runDir 'game'
    $libraries = Join-Path $Cache 'libraries'; $assets = Join-Path $Cache 'assets'
    New-Item -ItemType Directory -Force $natives, "$game\mods", $libraries, $assets | Out-Null
    foreach ($stale in 'logs', 'crash-reports', 'nows-fixture-results.txt', 'config', 'nows') { Remove-Item (Join-Path $game $stale) -Recurse -Force -ErrorAction SilentlyContinue }
    Get-ChildItem "$game\mods" -Filter *.jar | Remove-Item -Force
    Get-ChildItem $game -Filter 'hs_err_pid*.log' | Remove-Item -Force

    $classpath = [System.Collections.Generic.List[string]]::new()
    foreach ($library in $meta.libraries) {
        if (-not (Test-LibraryRules $(if ($library.PSObject.Properties['rules']) { $library.rules })) ) { continue }
        $artifact = $library.downloads.artifact
        if ($artifact -and $artifact.path -notmatch 'natives-windows-(x86|arm64)') {
            $file = Join-Path $libraries $artifact.path
            Get-VerifiedFile $artifact.url $file $artifact.sha1
            $classpath.Add($file)
        }
        if ($library.PSObject.Properties['natives'] -and $library.natives.PSObject.Properties['windows']) {
            $native = $library.downloads.classifiers.($library.natives.windows -replace '\$\{arch\}', '64')
            if ($native) {
                $file = Join-Path $libraries $native.path
                Get-VerifiedFile $native.url $file $native.sha1
                $zip = [IO.Compression.ZipFile]::OpenRead($file)
                foreach ($entry in $zip.Entries | Where-Object { $_.Name -match '\.dll$' }) {
                    [IO.Compression.ZipFileExtensions]::ExtractToFile($entry, (Join-Path $natives $entry.Name), $true)
                }
                $zip.Dispose()
            }
        }
    }
    $index = Join-Path $assets "indexes\$($meta.assetIndex.id).json"
    Get-VerifiedFile $meta.assetIndex.url $index $meta.assetIndex.sha1
    (Get-Content $index -Raw | ConvertFrom-Json).objects.PSObject.Properties.Value | Sort-Object hash -Unique |
        ForEach-Object -ThrottleLimit 16 -Parallel {
            $dest = Join-Path $using:assets "objects\$($_.hash.Substring(0, 2))\$($_.hash)"
            if (-not (Test-Path $dest)) {
                New-Item -ItemType Directory -Force (Split-Path $dest) | Out-Null
                Invoke-WebRequest "https://resources.download.minecraft.net/$($_.hash.Substring(0, 2))/$($_.hash)" -OutFile $dest -UseBasicParsing
            }
        }
    $log4j = $null
    if ($meta.PSObject.Properties['logging'] -and $meta.logging.client) {
        $log4j = Join-Path $Cache "log4j-$($meta.logging.client.file.id)"
        Get-VerifiedFile $meta.logging.client.file.url $log4j $meta.logging.client.file.sha1
    }
    if ($modJar) { Copy-Item $modJar "$game\mods\" }

    $adapterJar = Get-BuiltJar "mc\$version" "nows-mc-$version"
    $clientJar = Join-Path $RepoRoot "mc\$version\build\nows\minecraft\$version\client-dev.jar"
    $arguments = @('-Xmx1G') + $(if ($log4j) { "-Dlog4j.configurationFile=$log4j" }) + @(
        "-Djava.library.path=$natives", "-Dnows.clientJar=$clientJar", "-Dnows.minecraftAdapterPath=$($adapterJar.FullName)",
        "-Dnows.minecraftVersion=$version", '-Dnows.side=client', '-Dnows.profileId=verify',
        '-cp', (($nowsClasspath + $classpath) -join ';'), 'space.nows.loader.runtime.NowsLauncher',
        '--username', 'NowsVerify', '--version', $version, '--gameDir', $game, '--assetsDir', $assets, '--assetIndex', $meta.assetIndex.id,
        '--uuid', '00000000-0000-0000-0000-000000000000', '--accessToken', '0', '--userType', 'legacy',
        '--versionType', 'release', '--width', '854', '--height', '480')
    $stdout = Join-Path $WorkDir "$version.stdout.log"; $stderr = Join-Path $WorkDir "$version.stderr.log"
    $process = Start-Process $java -ArgumentList $arguments -WorkingDirectory $game -RedirectStandardOutput $stdout -RedirectStandardError $stderr -PassThru -WindowStyle Minimized
    return @{ Process = $process; Game = $game; Stdout = $stdout; Stderr = $stderr; Java = $java; AdapterJar = $adapterJar }
}

function Read-AllLogs($launch) {
    $parts = foreach ($file in $launch.Stdout, $launch.Stderr, (Join-Path $launch.Game 'logs\latest.log')) {
        if (Test-Path $file) { Get-Content $file -Raw }
    }
    ($parts | Where-Object { $_ }) -join "`n"
}

function Wait-ForMarkers($launch, [string[]] $markers) {
    $clock = [Diagnostics.Stopwatch]::StartNew()
    $menuSeconds = $null
    while ($clock.Elapsed.TotalSeconds -lt $LaunchTimeoutSec) {
        if ($launch.Process.HasExited) { return @{ Ok = $false; Reason = "the game exited with code $($launch.Process.ExitCode) before the markers appeared"; MenuSeconds = $menuSeconds } }
        $text = Read-AllLogs $launch
        if ($null -eq $menuSeconds -and -not @($MenuMarkers | Where-Object { $text -notmatch $_ })) { $menuSeconds = [Math]::Round($clock.Elapsed.TotalSeconds, 1) }
        $missing = @($markers | Where-Object { $text -notmatch $_ })
        if ($missing.Count -eq 0) { return @{ Ok = $true; MenuSeconds = $menuSeconds } }
        Start-Sleep -Seconds 1
    }
    @{ Ok = $false; Reason = "timed out after ${LaunchTimeoutSec}s (hang), missing: $((@($markers | Where-Object { (Read-AllLogs $launch) -notmatch $_ })) -join '; ')"; MenuSeconds = $menuSeconds }
}

# The fixture's own areas that need a loaded world are reported as not exercised. Anything else must pass.
$AlwaysNotExercised = @('networking.transport', 'players.current', 'events.serverTick', 'events.levelTick', 'registry.blockEntityAndMenu')
$NotExercisedFrom26 = @('registry.itemStack', 'registry.blockItemStack', 'recipeViewer.catalystStack', 'datagen.recipeAccepted')   # item components bind on world join

$results = @{}      # adapter -> api -> status
$startup = @{}
foreach ($version in $Versions) {
    $scope = "adapter $version"
    Write-Host "`n== $version" -ForegroundColor Cyan
    $adapterJar = Test-Artifacts $version
    if (-not $adapterJar) { continue }

    $modJar = $null
    if (-not $NoMod) {
        $output = Invoke-Gradle @('-q', '-p', (Join-Path $RepoRoot 'fixtures\consumer'), 'jar', "-Pminecraft_version=$version", "-Pnows_version=$nowsVersion")
        $modJar = Get-ChildItem (Join-Path $RepoRoot 'fixtures\consumer\build\libs') -Filter 'nows_consumer_fixture-*.jar' -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -notmatch 'sources' } | Select-Object -First 1
        if ($LASTEXITCODE -ne 0 -or -not $modJar) { $output | Select-Object -Last 30 | ForEach-Object { Write-Host $_ }; Fail $scope 'the consumer fixture does not build through the Gradle plugin'; continue }
        Test-FixtureJar $version $modJar.FullName
    }

    $launch = Start-Adapter $version ($modJar ? $modJar.FullName : $null)
    if (-not $launch) { continue }
    try {
        $markers = $MenuMarkers + $(if ($NoMod) { @() } else { @('NOWS_FIXTURE_SUMMARY') })
        $wait = Wait-ForMarkers $launch $markers
        if (-not $wait.Ok) { Fail $scope $wait.Reason } else { Pass $scope "reached the main menu in $($wait.MenuSeconds)s$($NoMod ? '' : ' and the fixture reported')" }
        $startup[$version] = $wait.MenuSeconds
        $text = Read-AllLogs $launch
        if ($text -notmatch [regex]::Escape("nows-mc-$version-$nowsVersion.jar")) { Fail $scope "the log does not show the adapter jar that was just built ($($launch.AdapterJar.Name)) being loaded" }
    } finally {
        if (-not $launch.Process.HasExited) { Stop-Process -Id $launch.Process.Id -Force; $launch.Process.WaitForExit() }
    }

    # Failure patterns over the complete logs, crash reports and JVM crash files.
    $logFiles = @($launch.Stdout, $launch.Stderr, (Join-Path $launch.Game 'logs\latest.log')) +
        @(Get-ChildItem (Join-Path $launch.Game 'crash-reports') -File -ErrorAction SilentlyContinue | ForEach-Object FullName) +
        @(Get-ChildItem $launch.Game -Filter 'hs_err_pid*.log' -ErrorAction SilentlyContinue | ForEach-Object FullName)
    $hits = 0
    $allowedSeen = [System.Collections.Generic.SortedSet[string]]::new()
    foreach ($file in $logFiles | Where-Object { Test-Path $_ } | Where-Object { (Split-Path $_ -Leaf) -ne ((Split-Path $launch.Stdout -Leaf)) }) {
        foreach ($entry in Get-LogEntries (Get-Content $file -Raw)) {
            if (-not (Test-LogEntry $entry)) { Fail $scope "failure pattern in $(Split-Path $file -Leaf): [$($entry.Level)] $($entry.Message)"; $hits++ }
            elseif ($entry.Message -match $FailurePattern -or @($entry.Lines | Where-Object { $_ -match $FailurePattern }).Count -gt 0) { [void]$allowedSeen.Add($entry.Message) }
        }
    }
    if ($hits -eq 0) { Pass $scope "failure-pattern scan found nothing outside the dummy-token allowlist (allowlisted entries seen: $($allowedSeen.Count))" }
    foreach ($seen in $allowedSeen) { Write-Host "     allowlisted: $seen" }

    # Fixture results.
    if (-not $NoMod) {
        $resultFile = Join-Path $launch.Game 'nows-fixture-results.txt'
        if (-not (Test-Path $resultFile)) { Fail $scope 'the fixture wrote no results file'; continue }
        $map = @{}
        foreach ($line in Get-Content $resultFile) {
            if ($line -match '^NOWS_FIXTURE api=(?<api>\S+) adapter=\S+ (?<status>PASS|FAIL|SKIPPED|NOT_EXERCISED)\s*(?<detail>.*)$') {
                $map[$Matches.api] = $Matches.status
                $allowed = ($AlwaysNotExercised -contains $Matches.api) -or ($version.StartsWith('26.') -and $NotExercisedFrom26 -contains $Matches.api)
                if ($Matches.status -eq 'FAIL' -or $Matches.status -eq 'SKIPPED') { Fail $scope "api $($Matches.api) $($Matches.status): $($Matches.detail)" }
                elseif ($Matches.status -eq 'NOT_EXERCISED' -and -not $allowed) { Fail $scope "api $($Matches.api) was not exercised: $($Matches.detail)" }
            }
        }
        $summary = Get-Content $resultFile | Where-Object { $_ -match '^NOWS_FIXTURE_SUMMARY ' }
        if (-not $summary) { Fail $scope 'the fixture wrote no summary line' }
        elseif ($summary -notmatch 'fail=0') { Fail $scope "fixture summary reports failures: $summary" }
        else { Pass $scope $summary }
        $results[$version] = $map
    }
}

# ---- 4. Startup time and parity -----------------------------------------------------------------------------------
if ($BaselineFile) {
    $baseline = Get-Content $BaselineFile -Raw | ConvertFrom-Json -AsHashtable
    foreach ($version in $startup.Keys | Sort-Object) {
        if ($baseline.ContainsKey($version) -and $startup[$version]) {
            $limit = $baseline[$version] * $MaxSlowdown + $SlowdownSlackSec
            if ($startup[$version] -gt $limit) { Fail "startup $version" "$($startup[$version])s to the main menu, baseline $($baseline[$version])s (limit $([Math]::Round($limit, 1))s)" }
            else { Pass "startup $version" "$($startup[$version])s to the main menu, baseline $($baseline[$version])s" }
        }
    }
}
if ($WriteBaselineFile) { $startup | ConvertTo-Json | Set-Content $WriteBaselineFile; Write-Host "wrote startup times to $WriteBaselineFile" }

if ($results.Count -gt 1) {
    $apis = @($results.Values | ForEach-Object { $_.Keys } | Sort-Object -Unique)
    foreach ($api in $apis) {
        $statuses = @($results.Keys | Sort-Object | ForEach-Object { "$_=$(if ($results[$_].ContainsKey($api)) { $results[$_][$api] } else { 'MISSING' })" })
        $distinct = @($statuses | ForEach-Object { $_.Split('=')[1] } | Sort-Object -Unique)
        $expectedSplit = $NotExercisedFrom26 -contains $api   # the one legitimate difference between adapters
        if ($distinct.Count -gt 1 -and -not $expectedSplit) { Fail 'parity' "$api differs between adapters: $($statuses -join ' ')" }
    }
    if (-not @($failures | Where-Object { $_.StartsWith('[parity]') })) { Pass 'parity' "$($apis.Count) APIs behave the same on $($results.Count) adapters" }
}

Write-Host ''
if ($failures.Count -gt 0) {
    Write-Host "FAILED: $($failures.Count) problem(s)" -ForegroundColor Red
    $failures | ForEach-Object { Write-Host "  $_" }
    exit 1
}
Write-Host "PASSED: $($Versions.Count) adapter(s) verified" -ForegroundColor Green
exit 0
