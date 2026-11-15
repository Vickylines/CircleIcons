# CircleIcons

Adapts legacy app icons into Android's adaptive icon pipeline, and fixes opaque notification icons showing up as solid white blocks in the status bar.

Tested on AOSP, Pixel, LineageOS, and other stock-like ROMs (Android 8.1 - 17).

[Releases](https://github.com/Vickylines/CircleIcons/releases) · [中文文档](README.md) · [Compatibility Matrix](docs/COMPATIBILITY.md)

![Android rendering preview](docs/preview.png)

## What it does

- **Legacy App Icon Adaptation**: Converts old, non-adaptive app icons into system-standard round adaptive icons on launchers and system settings.
- **Fix Notification White Squares**: Android status bar requires transparent, single-color icons. Non-standard apps that supply opaque notification icons often appear as solid white blocks. CircleIcons automatically extracts the silhouette and turns them into monochrome glyphs, preserving SystemUI light/dark tinting and animations.

## Supported Areas

- **Launchers**: Pixel Launcher, Launcher3, and AOSP-derived launchers.
- **System UI**: Status bar notification icons, Settings, package installer, share sheets, DocumentsUI.

*Note: Existing standard adaptive icons, dynamic icons, and avatars are left untouched.*

## Getting Started

> **Requirement**: Root access and an Xposed-compatible framework (e.g. LSPosed).

1. Download and install the latest APK from [Releases](https://github.com/Vickylines/CircleIcons/releases).
2. Enable CircleIcons in LSPosed.
3. Check the scope for **System UI**, your active **Launcher**, and **Settings**.
4. Reboot your device (or restart the hooked processes).

## Compatibility

- Built for AOSP, Pixel, LineageOS, and stock-like ROMs (Android 8.1–17).
- Heavily customized OEM skins (MIUI/HyperOS, One UI, ColorOS) are not officially supported.
- See [docs/COMPATIBILITY.md](docs/COMPATIBILITY.md) for detailed test results.

## Build

Requirements: JDK 17, Android SDK 36.

```sh
./gradlew :app:assemblePhoneRelease
```

## License

[Apache-2.0](LICENSE)
