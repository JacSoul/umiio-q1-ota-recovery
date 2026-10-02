# Umiio Q1 Repair Control 2.0.0

**Initial public pre-release — device validation of this cleaned-up binary is pending.** Package: `local.umiioq1.repaircontrol`; versionCode: `5`; minimum API: 23; target API: 28. Wi-Fi connection changes are enabled only on Android 9/API 28.

## English

This is the cleaned-up helper for the Q1 recovery workflow the owner successfully used. It is not a firmware update or one-click geometry repair.

- **Fix:** remove direct manual Keystone entry. **Open Projection Settings** launches only `com.telanda.keystone.MainActivity`; choose Keystone inside that OEM menu.
- Read-only Zoom/corner/canvas status, `Geometry` comparison and separate Q1 reference match. Scale is derived from raw, not independently read from OEM process memory.
- Explicit Wi-Fi preview and confirmation; generic user-entered SSID/password; WPA/WPA2 Personal or explicitly selected open network. WifiManager return values, connected broadcast, matching SSID and nonzero IPv4 are checked. No embedded personal credentials.
- Russian/English UI based on device language.
- Removed old direct-entry, Developer Options, reset and PC-repair actions. No root, ADB, HAL/property writes, firmware, system APK changes or reboot.
- No third-party APKs or OEM dumps included. Bluetooth deferred.

**Validation:** build completed, 26 pure Java tests and 14 source-audit checks passed; APK signature (v1/v2/v3) and alignment verified; manifest inspected; signing certificate matches the previous helper. **This specific 2.0.0 binary has not yet been run on Q1.** The owner-confirmed recovery was performed with earlier tools and the correct OEM menu sequence. Q2 and other models/revisions remain untested. Details: [VALIDATION.md](docs/VALIDATION.md).

**Known limitations:** some firmware denies property/SSID reads; unknown is not success. IPv4 is not an Internet test. Wi-Fi failure may leave a saved configuration. OEM MENU Reset remains unsafe on the affected build; do not use it. Correct numbers never replace visual inspection.

## Русский

Чистая версия помощника для уже сработавшего на Q1 способа. Это не прошивка и не автоматическая починка геометрии.

- Удалён прямой запуск ручного Keystone. **Open Projection Settings** открывает только главное меню MainActivity; пункт Keystone выбирается внутри него.
- Статус Zoom/углов/размеров — только чтение. Отдельно проверяется reference Q1 1024×600. Scale вычисляется из raw.
- Wi-Fi: своя сеть и пароль, предварительный показ изменений и подтверждение, проверка события подключения/SSID/IPv4. Личных сетей в APK нет.
- Интерфейс RU/EN; старые Developer Options/ADB/reset-действия удалены. Bluetooth пока отсутствует.
- Подпись прежняя, versionCode повышен для обновления существующего helper.

Сборка, тесты логики, статический аудит, подпись и выравнивание прошли. **Именно этот APK 2.0.0 ещё не проверялся на проекторе.** Уже подтверждён сам способ восстановления на Q1; совместимость Q2 не заявляется. Не меняйте восстановленные углы ради тестирования новой версии. APK публикуется как предварительный релиз с этим ограничением.

## Artifact integrity

File: `UmiioQ1RepairControl.apk`

```text
SHA-256
6E379DF13A3A77BEE653AB61E81BB591EB218DF7AC57B8656574044D3CAC8B85
```

Signing certificate SHA-256 (not the APK hash):

```text
22E4308D88494D8E18491D851403DA7F740F2BB5DF4209EBE3BB134570E86BE6
```

The private signing key is not included. Release attachments: APK and its matching `SHA256SUMS.txt`. [README RU](README.ru.md) · [README EN](README.md) · [Credits](CREDITS.md).
