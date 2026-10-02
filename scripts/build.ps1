param(
    [Parameter(Mandatory=$true)][string]$SdkRoot,
    [Parameter(Mandatory=$true)][string]$JdkRoot,
    [Parameter(Mandatory=$true)][string]$KeyStore,
    [string]$KeyAlias = 'umiioq1repaircontrol',
    [string]$BuildToolsVersion = '35.0.0'
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$Tools = Join-Path $SdkRoot "build-tools/$BuildToolsVersion"
$AndroidJar = Join-Path $SdkRoot 'platforms/android-28/android.jar'
foreach ($p in @($AndroidJar, $KeyStore, (Join-Path $JdkRoot 'bin/javac.exe'), (Join-Path $Tools 'aapt2.exe'))) {
    if (-not (Test-Path -LiteralPath $p)) { throw "Missing build input: $p" }
}
if (-not $env:UMIIO_STORE_PASSWORD -or -not $env:UMIIO_KEY_PASSWORD) {
    throw 'Set UMIIO_STORE_PASSWORD and UMIIO_KEY_PASSWORD privately. Never commit signing keys or passwords.'
}
# Every run gets its own directory. No recursive deletion, no download and no device calls.
$Run = Join-Path $Repo ('build/' + [guid]::NewGuid().ToString('N'))
$Classes = Join-Path $Run 'classes'
$Dex = Join-Path $Run 'dex'
$Tests = Join-Path $Run 'tests'
$Dist = Join-Path $Repo 'dist'
New-Item -ItemType Directory -Path $Classes,$Dex,$Tests,$Dist -Force | Out-Null
function Run-Checked([string]$Exe,[string[]]$Arguments) {
    & $Exe @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Exe failed with exit $LASTEXITCODE" }
}
$OldJavaHome = $env:JAVA_HOME
try {
    $env:JAVA_HOME = [IO.Path]::GetFullPath($JdkRoot)
    $JavaC = Join-Path $JdkRoot 'bin/javac.exe'
    $Java = Join-Path $JdkRoot 'bin/java.exe'
    $Jar = Join-Path $JdkRoot 'bin/jar.exe'
    $Sources = @(Get-ChildItem (Join-Path $Repo 'app/src') -Filter '*.java' -Recurse | ForEach-Object FullName)
    Run-Checked $JavaC (@('-encoding','UTF-8','--release','8','-classpath',$AndroidJar,'-d',$Classes) + $Sources)
    Run-Checked $JavaC @('-encoding','UTF-8','--release','8','-d',$Tests,
        (Join-Path $Repo 'app/src/local/umiioq1/repaircontrol/RecoveryRules.java'),(Join-Path $Repo 'tests/RecoveryRulesTest.java'))
    Run-Checked $Java @('-cp',$Tests,'RecoveryRulesTest')
    $ClassesJar = Join-Path $Run 'classes.jar'
    Run-Checked $Jar @('cf',$ClassesJar,'-C',$Classes,'.')
    Run-Checked (Join-Path $Tools 'd8.bat') @('--min-api','23','--lib',$AndroidJar,'--output',$Dex,$ClassesJar)
    $Resources = Join-Path $Run 'resources.zip'
    Run-Checked (Join-Path $Tools 'aapt2.exe') @('compile','--dir',(Join-Path $Repo 'app/res'),'-o',$Resources)
    $Unsigned = Join-Path $Run 'unsigned.apk'
    Run-Checked (Join-Path $Tools 'aapt2.exe') @('link','-o',$Unsigned,'-I',$AndroidJar,'--manifest',(Join-Path $Repo 'app/AndroidManifest.xml'),$Resources)
    Run-Checked $Jar @('uf',$Unsigned,'-C',$Dex,'classes.dex')
    $Aligned = Join-Path $Run 'aligned.apk'
    Run-Checked (Join-Path $Tools 'zipalign.exe') @('-p','4',$Unsigned,$Aligned)
    $Signed = Join-Path $Run 'UmiioQ1RepairControl.apk'
    Run-Checked (Join-Path $Tools 'apksigner.bat') @('sign','--ks',$KeyStore,'--ks-key-alias',$KeyAlias,
        '--ks-pass','env:UMIIO_STORE_PASSWORD','--key-pass','env:UMIIO_KEY_PASSWORD','--out',$Signed,$Aligned)
    Run-Checked (Join-Path $Tools 'apksigner.bat') @('verify','--verbose','--print-certs',$Signed)
    Run-Checked (Join-Path $Tools 'zipalign.exe') @('-c','4',$Signed)
    $Artifact = Join-Path $Dist 'UmiioQ1RepairControl.apk'
    Copy-Item -LiteralPath $Signed -Destination $Artifact -Force
    $Hash = (Get-FileHash -LiteralPath $Artifact -Algorithm SHA256).Hash
    [IO.File]::WriteAllText((Join-Path $Dist 'SHA256SUMS.txt'),"$Hash  UmiioQ1RepairControl.apk`n",[Text.UTF8Encoding]::new($false))
    Write-Output "Artifact: $Artifact"
    Write-Output "SHA-256: $Hash"
} finally { $env:JAVA_HOME = $OldJavaHome }
