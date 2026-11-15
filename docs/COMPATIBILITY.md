# Compatibility and verification

Version: **3.0.0-beta.1**. Checked on **2026-11-15**.

CircleIcons uses the traditional Xposed API 82. The module's minimum SDK is 27 (Android 8.1). Its hooks target AOSP-derived interfaces through Android 17 / API 37, but the framework must independently support the device's Android version. Installing an APK is not proof of a working hook.

## Evidence

| Environment | Android rendering checks | Actual system / launcher API inspection | Live Xposed behavior |
| --- | --- | --- | --- |
| AOSP emulator, Android 8.1 / API 27 | 6 passed | 3 passed; old LauncherIcons and String cache state found | Not run |
| AOSP emulator, Android 15 / API 35 | 6 passed | 3 passed; BaseIconFactory and two-argument String cache state found | Not run |
| Pixel 4 XL, LineageOS, Android 17 / API 37 | 6 passed | 3 passed; structured launcher state and notification cache schema found | Previous 2.3 device build tested; 3.0 beta runtime not yet reloaded |
| AOSP CI emulator, Android 11 / API 30 | 6 passed | 3 passed; Launcher3 artwork and cache targets found | Not run |
| Other Android 9–14 and 16 AOSP builds | Not run | Selected upstream source signatures reviewed | Not run |
| Google's Pixel Launcher | Shared AOSP path implemented | Package alias added; private implementations not device-tested | Not run |
| Heavily customized OEM ROMs / third-party launchers | Not tested | No blanket compatibility claim | Not tested |

The saved [API 27](verification/android-27.txt), [API 30](verification/android-30.txt), [API 35](verification/android-35.txt) and [API 37](verification/android-37.txt) runner results contain only test names and outcomes. [CI run 43696442478](https://github.com/Vickylines/CircleIcons/actions/runs/43696442478) passed the clean build, Lint and all 9 tests on each of API 27, 30 and 35 for code commit `d1aa48dbf1a71392a1d8e46842e69c0b2448556d`. Subsequent release-preparation commits only update documentation and saved evidence. [CI](https://github.com/Vickylines/CircleIcons/actions/workflows/android.yml) adds clean build/Lint and Android 8.1, 11 and 15 checks on every source update.

The rendering checks exercise native adaptive-icon identity and mask coverage; transparent, empty and solid notification artwork; both monochrome tints; animation/level preservation; hardware bitmap input; and bounded output for large resources. Inspection resolves methods from the installed SystemUI and launcher APKs, not mock classes. It also checks launcher cache methods and the newer notification cache schema where present. **These tests do not inject or execute Xposed hooks.**

The preview in the README is produced by Android Canvas on the API 37 device, using the same conversion code and synthetic test artwork. A ROM's own mask determines the adaptive shape.

## Capability map

| Surface | Older implementation | Newer implementation | When absent |
| --- | --- | --- | --- |
| System app artwork | `loadUnbadgedItemIcon` and declared application resources | Also supports `getDrawableInternal(..., isIcon)` | Logged installation failure; other features continue |
| Launcher artwork | `graphics.LauncherIcons.createBadgedIconBitmap` | `BaseIconFactory.normalizeAndWrapToAdaptiveIcon` / `wrapToAdaptiveIcon`, matching available overloads | PackageManager path remains available; launcher-specific feature logged unavailable |
| Launcher cache | String state from `IconProvider` | `LauncherIconProviderImpl` / `PersistedItemState.withAdditionalValues(String[])` | Logged unavailable; no destructive database reset |
| Status-bar small icon | Instance `StatusBarIconView.getIcon(StatusBarIcon)` | Same entry, with optional conversation-avatar exclusion | Status-bar feature logged unavailable; system symbols remain untouched |
| Notification cache | No additional provider/cache on these versions | Resolved provider context, standard/skeleton caches and Map storage | Feature skipped; native older notification behavior remains |
| Resolver | Framework `SimpleIconFactory` | IntentResolver `SimpleIconFactory` | PackageManager path remains available |
| Splash | No shell splash provider before Android 12 | `IconProvider.getIcon` accepting activity/component/app info | Feature skipped |

Native adaptive drawables are returned unchanged. Existing icons with square artwork embedded inside their adaptive foreground are not automatically redrawn. Legacy icons with little or no contrast cannot always yield a recognizable monochrome glyph. Correct alpha-based notification icons, animated icons, level lists and conversation avatars are preserved. General grayscale conversion cannot infer a missing logo from a featureless solid bitmap.

Framework/root requirements, ROM-selected mask shape and untested proprietary implementations limit the supported scope. New future Android releases may rename or replace these private APIs; logs and the issue template exist to track such changes rather than claim permanent compatibility.

## Primary references

- [Android notification design](https://developer.android.com/design/ui/mobile/guides/home-screen/notifications): status-bar small icons and system presentation.
- [AOSP Android 8.1 StatusBarIconView](https://android.googlesource.com/platform/frameworks/base/+/android-8.1.0_r1/packages/SystemUI/src/com/android/systemui/statusbar/StatusBarIconView.java).
- [AOSP Android 14 IconProvider](https://android.googlesource.com/platform/frameworks/libs/systemui/+/android-14.0.0_r1/iconloaderlib/src/com/android/launcher3/icons/IconProvider.java).
- [AOSP Android 16 BaseIconFactory](https://android.googlesource.com/platform/frameworks/libs/systemui/+/android-16.0.0_r1/iconloaderlib/src/com/android/launcher3/icons/BaseIconFactory.java).
- [LSPosed](https://github.com/LSPosed/LSPosed) and [Vector's legacy API implementation](https://github.com/JingMatrix/Vector/blob/master/legacy/README.md): framework requirements are separate from module SDK support.
- [AGP 8.13 release documentation](https://developer.android.com/build/releases/agp-8-13-0-release-notes): build toolchain requirements.
