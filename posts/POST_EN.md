# Umiio Q1 OTA recovery: launcher, Wi-Fi and full Keystone without flashing

If a stock Umiio Q1 update left you with a white screen, missing Wi-Fi/Keystone settings, or an apparently half-bricked projector, do not rush into flashing firmware or a factory reset. I recovered the launcher, Wi-Fi and full Keystone without root or reflashing.

**Tested on my Umiio Q1 running Android 9. Not verified on Q2 or other revisions.** This is a working UI recovery workaround, not a universal OTA fix.

What helped:

1. Restore a usable launcher. I use **Projectivy Launcher**. Credit for the initial Android TV remote/app-store escape route goes to **mopen_popen**, in [this original Mail Answers post](https://otvet.mail.ru/question/269073066). That entry route requires the phone remote to reach the projector.
2. Connect Wi-Fi using our **Repair Control / WifiManager**, bypassing crashing TvSettings. Enter your own network, approve the change, and wait for a confirmed SSID/IPv4. Connecting Wi-Fi does not change projection geometry.
3. **The crucial Keystone fix: open `com.telanda.keystone.MainActivity` first**, choose its stock **Keystone** menu, then adjust the corners manually.

```text
Wrong:   launcher / old RepairControl → direct KeystoneActivity
Correct: ProjectionSettings MainActivity → Keystone menu → KeystoneActivity
```

Direct manual-activity entry in a fresh process skipped required dimension/bound initialization. Even with Zoom reported as 100%, corners moved inward but not outward. Our older helper used that wrong direct entry too. The final helper removes it: **Open Projection Settings** launches only MainActivity.

If you have already used a direct shortcut, do a normal full restart, then enter through the main menu. **Do not press MENU Reset:** it produced incorrect coordinates on my affected OTA. No Auto_set_zero, FactoryMode, ADB/JDWP, root or firmware flashing was needed.

After correct entry and manual adjustment, my full rectangle was restored:

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

This is my Q1's reference, **not coordinates to force onto Q2**. Visually inspect the image too: the displayed scale is derived from a property, not independently measured hardware state.

Source, RU/EN instructions, APK and SHA-256: [umiio-q1-ota-recovery](https://github.com/JacSoul/umiio-q1-ota-recovery). Read the release notes for the testing status of that specific APK. The helper is not an automatic system repair: it reads status, opens the correct OEM menu and connects Wi-Fi after confirmation.

No third-party APKs are reuploaded: get [Projectivy from Google Play](https://play.google.com/store/apps/details?id=com.spocky.projengmenu) or its [official releases](https://github.com/spocky/miproja1/releases). Activity Launcher is optional; if used, choose MainActivity, never direct manual Keystone.

If MainActivity crashes or outward movement is still blocked after correct entry, stop rather than repeatedly resetting. Another OTA/hardware revision may have a different problem. [Related Q2 reports on 4PDA](https://4pda.to/forum/index.php?showtopic=1064988&st=1180) are context, not evidence of compatibility.
