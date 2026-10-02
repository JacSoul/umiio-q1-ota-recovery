# Build from source

Windows PowerShell, JDK 17, Android SDK Platform 28 and Android Build Tools 35.0.0 are required. SDK/JDK and signing keys are **not** bundled. The build script makes no downloads and does not contact a projector.

```powershell
# Set these two environment variables privately, outside a committed script:
# UMIIO_STORE_PASSWORD and UMIIO_KEY_PASSWORD
./scripts/build.ps1 -SdkRoot 'C:\Android\Sdk' -JdkRoot 'C:\Java\jdk-17' -KeyStore 'C:\Private\release.jks' -KeyAlias 'your-alias'
```

Use your own keystore to build a fork. The release signing key is intentionally private. A differently signed build cannot update the supplied helper in place. Do not attempt to replace or uninstall an OEM system app.

The script compiles Java 8-compatible code, runs the pure Java validation tests, creates DEX with D8 for min API 23, compiles/links resources, zip-aligns, signs, verifies the signature/alignment and computes SHA-256. Wi-Fi mutation is guarded to Android 9/API 28 in application code.

Outputs:

```text
dist/UmiioQ1RepairControl.apk
dist/SHA256SUMS.txt
```

`build/` contains isolated per-run intermediates. `build/`, `dist/`, APKs and signing keys are ignored by Git. No destructive cleanup is performed. Paths containing spaces are passed as separate arguments. Passwords are supplied to apksigner through environment references, not command-line literals; clear the variables after building.

Archive timestamps/tool versions/signing configuration can change the binary hash between builds. This project does not claim bit-for-bit reproducible APKs. Verify the hash of the **specific downloaded release** against its published manifest.

Для самостоятельной сборки используйте свой ключ. Ключ релиза и личные пароли не публикуются. Самостоятельная сборка с другой подписью не является совместимым обновлением установленного APK из релиза. Проверки сборки не заменяют тестирование на проекторе.
