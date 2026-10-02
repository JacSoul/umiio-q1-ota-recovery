# What actually fixed Keystone

## Observed on the owner's Q1

- Reported Zoom was already raw=1000 / derived scale=1.000.
- When manual Keystone was launched directly, inward adjustment worked but outward movement was blocked.
- Entering through `com.telanda.keystone.MainActivity`, then selecting the stock Keystone menu, restored outward adjustment.
- Manual adjustment then restored LT=0,600; RT=1024,600; LB=0,0; RB=1024,0 and a visually full-size image.

```text
Wrong:   launcher / old helper → KeystoneActivity (fresh process)
Correct: ProjectionSettings MainActivity → stock Keystone menu → KeystoneActivity
```

## Interpretation and limits

The inspected OEM code initializes screen dimensions through its main activity, and the manual correction helper uses cached dimensions/step bounds. Direct entry can initialize the manual path before the required dimensions. A Zoom property of 1000 is not evidence that those in-process bounds are initialized. Reading properties alone cannot show the contents of the OEM process's static fields.

The old helper's direct manual-activity button was therefore part of the problem, not a safe shortcut. It is removed, with no fallback to the manual activity. This explanation concerns the tested initialization failure; it does not establish the cause of every OTA failure or every projection defect.

The recovery does **not** need a new system app, privileged bridge or raw property write. The user enters the existing OEM main menu and uses its normal manual controls. If the manual helper was already initialized incorrectly, a normal full restart is the conservative way to discard process-local cached state; standby/Back alone does not guarantee that. The helper itself never reboots the projector.

MENU Reset produced unexpected coordinates during the investigation and must not be used on the affected build. Do not use it as a shortcut to the rectangle. Auto_set_zero and FactoryMode are not required.

## Reference, not a write payload

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

These are the owner's final Q1 values, not defaults to force onto Q2 or another panel. The public helper contains no code to write them. `scale` is raw/1000, not an independent measurement of the display pipeline. Always inspect the physical image.

## Кратко по-русски

Исправление оказалось в порядке открытия штатных экранов: **сначала MainActivity, затем пункт Keystone**. Старый прямой запуск ручного экрана мог пропустить инициализацию размеров и оставить некорректные границы в процессе. Поэтому 100% Zoom не гарантировали движение наружу. Владелец подтвердил восстановление после правильного входа и ручной настройки. Если прямой вход уже использовался, выполните обычную полную перезагрузку и начните с главного меню. **MENU Reset не нажимать.** Это не рекомендация записывать координаты напрямую и не доказательство совместимости Q2.
