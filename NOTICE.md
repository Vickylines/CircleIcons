# Notices

`app/src/main/java/com/yusd/circleicons/MonochromeIconFactory.java` derives from Android Open Source Project / LineageOS.

Copyright (C) 2024 The Android Open Source Project. Licensed under the Apache License, Version 2.0.

Upstream: https://github.com/LineageOS/android_frameworks_libs_systemui/blob/lineage-22.2/iconloaderlib/src/com/android/launcher3/icons/MonochromeIconFactory.java

Local modifications: package namespace; removal of build-only AndroidX annotations; an unmasked inset wrapper for notification glyphs instead of a launcher tile mask; PorterDuff SRC in place of BlendMode SRC for Android 8.1 compatibility. Grayscale projection, contrast scaling and background polarity detection retain the upstream algorithm.

The full license is in `LICENSE` and is also bundled in the APK at `assets/licenses/AOSP.txt`.

Gradle Wrapper is distributed by the Gradle project under Apache-2.0. Its JAR and distribution are verified by the upstream SHA-256 checksums pinned during setup. Xposed API is a compile-only dependency and is not bundled in the release APK.
