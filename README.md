# Umiio Q1 OTA Recovery — white screen, Wi-Fi and Keystone fix

[Русский](README.ru.md) · [Release notes](RELEASE_NOTES.md) · [Build from source](docs/BUILD.md) · [Credits](CREDITS.md)

If a stock OTA update left your Umiio Q1 with a white screen, missing Wi-Fi/Keystone settings or an apparently half-bricked interface, do not rush into flashing or a factory reset. One owner's Q1 recovered its launcher, Wi-Fi and full-size Keystone without root or reflashing.

**Unofficial, device-specific recovery workflow — not firmware and not a universal repair.** The owner reports a successful recovery on their **Umiio Q1, Android 9**. **Q2 and other Umiio models/revisions are untested.** Similar symptoms do not establish compatible hardware, packages or coordinate limits.

## What is actually verified?

| Item | Evidence / scope |
|---|---|
| Android still running behind the broken UI after OTA | Reported and observed during recovery of this Q1 |
| Working home screen with Projectivy Launcher | Confirmed by the Q1 owner |
| Wi-Fi through Repair Control / `WifiManager`, bypassing TvSettings | Confirmed with the earlier helper on that Q1 |
| Manual Keystone after entering through ProjectionSettings main menu | Owner confirms outward movement and full rectangle restored |
| Reference values below | Owner-confirmed read-back and visual recovery |
| Cleaned-up APK 2.0.0 | Local build/testing status is listed in [VALIDATION.md](docs/VALIDATION.md); not yet tested on Q1 |
| Q2 / another Q1 hardware or OTA revision | **Not verified** |

This is a workaround for the affected interface and its initialization path. It does not replace the broken system settings app or prove which OTA component originally caused every failure. The old direct-launch helper also introduced an avoidable Keystone initialization problem; the final app removes that path.

## What actually fixed Keystone

```text
Wrong:   launcher / old RepairControl → direct KeystoneActivity
Correct: ProjectionSettings MainActivity → Keystone menu → KeystoneActivity
```

The correct entry is:

```text
com.telanda.keystone.MainActivity
```

Do **not** create a shortcut directly to `com.telanda.keystone.KeystoneActivity`.

In the inspected OEM code, the main menu initializes screen dimensions before the manual helper caches its bounds. Direct manual-activity entry in a fresh process can skip this initialization. A displayed `raw=1000` does not repair those bounds. After entering through the main menu, this Q1's corners could be moved outward again. See [the technical summary](docs/KEYSTONE.md).

If you already used direct entry in the current process, do a **normal full restart**, then open the main menu first. Standby or just pressing Back does not guarantee a fresh process. Do not clear app data or repeat resets.

## Recovery steps

### 1. Regain access to a launcher

If a file manager, app store or existing launcher is accessible, use it to install/open Projectivy Launcher from an official source:

- [Projectivy on Google Play](https://play.google.com/store/apps/details?id=com.spocky.projengmenu)
- [Projectivy developer's GitHub Releases](https://github.com/spocky/miproja1/releases)

Use Projectivy's documented home-screen setup; do not delete or disable system packages. Engineering/calibration features offered by other apps are not part of this recovery.

For an otherwise white-screen Q1 that is still reachable from a phone, **mopen_popen's original post** describes an Android TV remote/app-store entry route: [“Белый экран после обновления проектора Umiio Q1”](https://otvet.mail.ru/question/269073066). It depends on the remote being able to reach the device; it is not an offline guarantee. Credit for that initial escape route belongs to mopen_popen, not this project.

If Android cannot boot or there is no way to open/install an app, this APK cannot bootstrap itself. Stop rather than trying unknown firmware or hidden factory screens.

### 2. Install Repair Control

Download from this repository's [GitHub Releases](https://github.com/JacSoul/umiio-q1-ota-recovery/releases). Use only the APK and matching `SHA256SUMS.txt` from the same release, not a similarly named third-party file. **The initial 2.0.0 APK is a pre-release: locally validated, not yet run on Q1.** The recovery workflow itself was confirmed by the Q1 owner with earlier tools.

Transfer `UmiioQ1RepairControl.apk` through your working file manager/network route or an ordinary USB drive and approve Android's normal installation prompt. This is an ordinary app install, not a bootable USB or firmware update. Do not format anything for this kit.

Verify on Windows:

```powershell
Get-FileHash .\UmiioQ1RepairControl.apk -Algorithm SHA256
```

Version 2.0.0 retains package `local.umiioq1.repaircontrol` and the previous local signing identity, with a higher versionCode. If Android reports a signature conflict, stop; do not remove system packages. A self-built APK signed with another key is not a compatible update of the supplied helper.

### 3. Restore Wi-Fi without TvSettings

1. Open Repair Control and enter **your own** exact SSID/password. No personal network is embedded.
2. Select WPA/WPA2 Personal, or explicitly choose an open network. WPA3-only and Enterprise are not supported by this helper. Prefer a 2.4 GHz network when band support is uncertain.
3. On Android 9, grant the requested Location permission if you want SSID-based verification; the app does not obtain GPS coordinates. If Android hides SSID, its location-service switch may also need to be enabled through a working UI.
4. Press **Connect Wi-Fi — preview changes**, read the plan, then confirm.
5. Wait for a matching SSID, nonzero IPv4 and `NETWORK_STATE_CHANGED_ACTION` confirmation. Returning `true` from `reconnect()` alone is not success.

The app uses `WifiManager`: add/update one matching SSID configuration → check network ID → save → enable/select → reconnect. Selecting it can disconnect the previous network. On failure a saved configuration can remain; no automatic rollback or retry is performed. The app's connection action is deliberately limited to Android 9/API 28.

Credentials are passed to Android, which saves Wi-Fi configuration. Repair Control does not write passwords to its own files/logs, and clears the password field. It does not use TvSettings, NetworkActivity, EthernetManager, ADB or a projection reset. See [privacy and permissions](docs/PRIVACY.md).

An IPv4 address confirms local configuration, not Internet connectivity. Client isolation can prevent local devices communicating even when both have Internet; a local hotspot is an alternative network, not a required repair step. ADB is not needed.

### 4. Restore projection geometry

1. If you previously opened manual Keystone directly, fully restart normally. Do not press MENU Reset.
2. In Repair Control press **Open Projection Settings**. It opens **only** `com.telanda.keystone.MainActivity`.
3. If Zoom is already `raw=1000`, leave it alone. Otherwise use the stock **Quick move / Zoom** item carefully; on the tested Q1 RIGHT increased Zoom. Verify `raw=1000`, then return to the main ProjectionSettings menu. Do not blindly apply this direction to another firmware.
4. Select **Keystone inside that main menu**. Choose each corner with the stock UI and adjust it manually. On the inspected Q1, OK cycles LT → RT → RB → LB; outward directions are LT ←/↑, RT →/↑, RB →/↓, LB ←/↓. A corner already at its boundary should not move farther.
5. Return to Repair Control and refresh the read-only values. Check the physical image too. Do not keep moving a corner inward when trying to enlarge the picture.

If a corner is malformed/out of range, keys behave differently, the main activity is missing, or outward movement remains blocked after correct entry, stop rather than guessing more changes. There is no one-click reset in this helper.

### Final reference state — tested Q1 only

```text
Zoom: 100%
scale=1.000
raw=1000

LT=0,600
RT=1024,600
LB=0,0
RB=1024,0

Geometry: FULL RECTANGLE
```

`scale` is derived from `raw/1000`; it is not a separate hardware measurement. `Geometry` checks the four read properties against the reported OEM canvas. `Q1 reference: MATCH` additionally requires 1024×600 and raw=1000. None of these replaces visual confirmation. Full digital canvas also does not fix optical placement/focus.

## Do NOT use for this procedure

- **MENU Reset** in the affected manual Keystone screen: it was not safe/reliable in this recovery.
- Direct `KeystoneActivity` shortcuts or the obsolete **Open Manual Keystone** action.
- `Auto_set_zero`, FactoryMode, engineering calibration, factory reset.
- Root, ADB/JDWP, vendor-property writes, firmware/OTA rollback or flashing.
- System APK replacement, deletion or signing-key experiments.

Old experimental collectors/exporters and PC repair kits are **not** release dependencies. Bluetooth is intentionally not implemented.

## Troubleshooting

| Symptom | Safe next step |
|---|---|
| White screen, but phone remote still connects | Follow the credited entry route; install a launcher from its official source |
| Main Projection Settings missing/crashing | Stop; this workflow is not verified for that package/firmware |
| Outward blocked after a direct shortcut | Normal full restart, then main menu first; no MENU Reset |
| Zoom says 100%, geometry still small | Zoom and corner bounds are separate; follow correct entry and inspect corners |
| `UNKNOWN` properties | This build may deny the hidden read API; do not treat unknown as zero or a successful repair |
| SSID unknown / connection unconfirmed | Check Android permission/location availability, exact SSID and network security; do not assume success |
| `addNetwork/updateNetwork = -1` | Configuration or permission was rejected; stop and check the network, not Keystone |
| Internet works but devices cannot see each other | Check client isolation; no ADB is required for this workflow |
| Coordinates match but image still looks wrong | Check placement/optics; numeric match is not a visual guarantee |
| Q2 or another Umiio | Treat as unverified; do not force Q1's 1024×600 onto another model |

## Credits and related reports

Thank you **mopen_popen** for the initial remote/app-store escape route: [original Mail Answers post](https://otvet.mail.ru/question/269073066). Thanks to **Spocky** for Projectivy Launcher and to the **Activity Launcher contributors**. Full attribution is in [CREDITS.md](CREDITS.md).

[Related Q2/OTA white-screen reports on 4PDA](https://4pda.to/forum/index.php?showtopic=1064988&st=1180) provide context, **not proof of Q2 compatibility**. Activity Launcher is optional: [Play Store](https://play.google.com/store/apps/details?id=de.szalkowski.activitylauncher), [official GitHub](https://github.com/ActivityLauncher/ActivityLauncher). If used, open only the ProjectionSettings **main** activity; the final helper makes Activity Launcher unnecessary for this step.

No third-party launcher APKs or OEM APK/OAT/VDEX/firmware dumps are redistributed. Original helper source and documentation: [MIT license](LICENSE). Names/trademarks belong to their respective owners; this is not an official Umiio product.

## Found this while searching for Umiio Q2?

Related reports mention Umiio Q2 OTA white-screen/settings failures, but this repository documents **one Q1 recovery only**. It does not provide a verified Q2 firmware fix. See the linked reports and compare your model/build before assuming the same initialization path. Do not force Q1 coordinates onto another panel.

## Reporting results safely

Use [GitHub Issues](https://github.com/JacSoul/umiio-q1-ota-recovery/issues) to report a model, Android version, symptoms and what worked. Do not post passwords, SSIDs, IP/MAC addresses, serial numbers, unredacted screenshots or full diagnostic collections. Search terms covered by this guide: Umiio Q1 white screen after update, broken settings, Wi-Fi recovery, Keystone outward movement blocked, projection size/zoom 100%.
