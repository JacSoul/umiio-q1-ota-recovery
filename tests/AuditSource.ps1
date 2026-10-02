$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$SourceFiles = @(Get-ChildItem (Join-Path $Repo 'app/src') -Recurse -Filter '*.java')
$Source = ($SourceFiles | ForEach-Object { Get-Content -LiteralPath $_.FullName -Raw }) -join "`n"
$Main = Get-Content (Join-Path $Repo 'app/src/local/umiioq1/repaircontrol/MainActivity.java') -Raw
$Rules = Get-Content (Join-Path $Repo 'app/src/local/umiioq1/repaircontrol/RecoveryRules.java') -Raw
function Assert-Check([bool]$Pass,[string]$Name) {
    if (-not $Pass) { throw "FAIL: $Name" }
    Write-Output "PASS: $Name"
}
Assert-Check ($Rules.Contains('com.telanda.keystone.MainActivity')) 'OEM target is MainActivity'
Assert-Check (-not $Source.Contains('com.telanda.keystone.KeystoneActivity')) 'No direct manual-activity target'
Assert-Check (([regex]::Matches($Main,'startActivity\(')).Count -eq 1) 'One activity launch call-site'
Assert-Check (-not ($Source -match 'SystemProperties\.set|hiPropertySet|HwBinder|Runtime\.getRuntime|ProcessBuilder|Settings\.Global\.put|setprop|ctl\.start')) 'No known property/HAL/shell writer calls'
Assert-Check ($Main.Contains('getMethod("get",String.class,String.class)')) 'Property reflection is getter-only'
Assert-Check ($Main.Contains('NETWORK_STATE_CHANGED_ACTION') -and $Main.Contains('!connectedEvent') -and $Main.Contains('info.getIpAddress()==0') -and $Main.Contains('!pendingSsid.equals')) 'Connection evidence gates present'
Assert-Check ($Main.Contains('networkId<0') -and $Main.Contains('!manager.saveConfiguration()') -and $Main.Contains('!manager.enableNetwork(networkId,true)') -and $Main.Contains('!manager.reconnect()')) 'Wi-Fi return values checked'
Assert-Check ($Main.Contains('Build.VERSION.SDK_INT!=28')) 'Connection restricted to API 28'
Assert-Check (-not ($Source -match 'FileOutputStream|FileWriter|SharedPreferences|android\.util\.Log|System\.out')) 'No app file/preferences/log writers'
[xml]$Manifest = Get-Content (Join-Path $Repo 'app/AndroidManifest.xml')
$ns = 'http://schemas.android.com/apk/res/android'
$Allowed = @('ACCESS_WIFI_STATE','CHANGE_WIFI_STATE','ACCESS_NETWORK_STATE','ACCESS_FINE_LOCATION') | ForEach-Object { 'android.permission.' + $_ }
$Actual = @($Manifest.manifest.'uses-permission' | ForEach-Object { $_.GetAttribute('name',$ns) })
Assert-Check ((Compare-Object $Allowed $Actual | Measure-Object).Count -eq 0) 'Only four documented permissions'
Assert-Check ($Manifest.manifest.GetAttribute('versionCode',$ns) -eq '5') 'Version code 5'
Assert-Check ($Manifest.manifest.application.GetAttribute('allowBackup',$ns) -eq 'false') 'Android app backup disabled'
Assert-Check ((@($Manifest.manifest.application.SelectNodes('activity|service|receiver|provider'))).Count -eq 1) 'One manifest component (launcher activity only)'
$Tokens = $null; $Errors = $null
$null = [Management.Automation.Language.Parser]::ParseFile((Join-Path $Repo 'scripts/build.ps1'),[ref]$Tokens,[ref]$Errors)
Assert-Check ($Errors.Count -eq 0) 'Build PowerShell parses'
Write-Output 'Static checks are not proof of runtime behavior or Android device testing.'
