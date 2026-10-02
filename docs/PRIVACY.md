# Privacy and permissions / Данные и разрешения

The app has no analytics, advertising SDK, backend, updater or Internet permission. It does not save diagnostic reports, take screenshots or export files. No personal SSID/password or device-specific IP is embedded.

| Permission | Purpose |
|---|---|
| ACCESS_WIFI_STATE | Read Wi-Fi state, saved configurations and connection information |
| CHANGE_WIFI_STATE | Explicitly approved enable/add/update/select/reconnect operation |
| ACCESS_NETWORK_STATE | Network-state access for the connection workflow |
| ACCESS_FINE_LOCATION | Android 9's SSID visibility requirement; requested at runtime before connection |

No GPS/location provider is accessed. Android may additionally require its location-service switch to reveal SSID. If the firmware hides SSID, the app reports an unconfirmed connection rather than claiming success.

The user types credentials for their own network. Password text is masked, excluded from view-state saving/autofill, and cleared when leaving the activity, confirming or cancelling the connection dialog. The app does not write it to its own preferences, files or logs. It necessarily exists transiently in process memory; secure memory erasure is not guaranteed. Android's Wi-Fi service retains saved credentials as part of its normal configuration. The keyboard/OS has its own data handling policies.

Wi-Fi connection changes are not read-only: they can enable Wi-Fi, update a matching SSID, disconnect the old network and leave a saved configuration even if later steps fail. A confirmation dialog explains this before changes. There is no automatic connection at startup or automatic retry. The success message records the **last confirmed connection**; refresh also displays current connection information.

Projection status reads `persist.display.keystone_scale` and `persist.sys.keystone.{width,height,lt,rt,lb,rb}` through the hidden SystemProperties **get** API. If unavailable, it displays unknown. The app does not invoke a setter, HAL, shell, root, calibration, reset or reboot. Open Projection Settings launches only OEM MainActivity after confirmation. Subsequent user actions in that OEM app are outside this helper's read-only status functionality.

На русском: приложение не собирает телеметрию и не отправляет отчёты. Пароли не вшиты и не записываются самим приложением, но передаются Android для сохранения сети. Разрешение Location используется для чтения SSID, не GPS. Подключение Wi-Fi меняет настройки сети только после подтверждения. Статус Zoom/Keystone — только чтение; координаты меняет сам пользователь внутри штатного меню. Не публикуйте отчёты/скриншоты со своими SSID, IP или другими личными данными без проверки.
