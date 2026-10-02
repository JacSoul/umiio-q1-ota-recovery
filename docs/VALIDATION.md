# Validation record — 2.0.0 / versionCode 5

Local checks performed 2026-10-01. No action was performed on the projector during this cleanup/build.

| Check | Result |
|---|---|
| JDK 17 compilation, Java 8 language/API target | PASS |
| D8 min API 23, Android API 28 library | PASS |
| Resource linking / signed APK generation | PASS |
| Pure Java unit tests | PASS, 26 assertions; not Android instrumentation |
| `tests/AuditSource.ps1` | PASS, 14 static checks; pattern assertions are not a formal safety proof |
| APK signature | PASS, v1/v2/v3, one signer |
| Alignment | PASS, zipalign check |
| Previous helper certificate comparison | MATCH |
| Manifest from built APK | Correct package, code 5 / 2.0.0, min23/target28, four documented permissions |
| External activity launch | One call-site, only `com.telanda.keystone.MainActivity`, no direct manual target |
| Original source privacy scan | No personal network name/IP, credential literal, private key or local user path found |
| Android/Q1 install and launch of this exact binary | **NOT RUN** |
| Wi-Fi connect using this exact binary | **NOT RUN**; earlier WifiManager workflow succeeded on owner's Q1 |
| Owner's previously recovered geometry | Confirmed by owner; not a new device measurement during this build |
| Q2 / other hardware or OTA revisions | **NOT TESTED** |

The source-audit script verifies the expected activity target, lack of known property/HAL/shell writer calls, getter-only reflection, connection evidence/return-value gates, API 28 guard, absence of file/preferences/log writers, permission set, versionCode, backup flag, component count and build-script syntax. It does not execute Android callbacks or emulate the OEM firmware.

Pure tests cover SSID byte length/control characters, WPA passphrase/hex PSK validation, quoting, integer parsing, rectangle checks, IPv4 formatting and the OEM main-activity constant.

## Minimal check of the new version before a public binary release

Do not undo the working repair to test the app. Install the same-signer helper update normally, confirm status loads, check the read-only reference and open **MainActivity only**, then return. Verify no direct manual/reset action exists. Wi-Fi can be tested only if the owner wants to change/reconnect a network; otherwise leave that runtime test marked pending. Never claim a pending test passed.

The prior successful Q1 recovery and a locally validated build are different evidence. The release notes intentionally keep that distinction.

## Integrity

APK SHA-256: `6E379DF13A3A77BEE653AB61E81BB591EB218DF7AC57B8656574044D3CAC8B85`

Certificate SHA-256: `22E4308D88494D8E18491D851403DA7F740F2BB5DF4209EBE3BB134570E86BE6`

На русском: локальная сборка и проверки прошли; новую версию на Q1 не запускали. Геометрию уже восстановил и подтвердил владелец ранее. Статус сборки нельзя выдавать за новый визуальный тест устройства.
