# Changelog

## 3.0.0-beta.1

- Replace Android-17-specific method assumptions with independent capability detection.
- Add Oreo/Pie Launcher3, older iconloaderlib and Pixel Launcher package paths.
- Support legacy String cache fingerprints and newer structured launcher state.
- Preserve status-bar tinting; support older SystemUI without conversation fields.
- Scope notification cache invalidation to ROMs that actually expose the required cache schema.
- Replace API-29-only blending with API-27-compatible PorterDuff compositing.
- Support hardware-backed bitmap input and bound generated status-icon bitmap size.
- Add Google variants of supported system packages to the recommended scope.
- Add reproducible Gradle builds, Android rendering/interface inspection, CI and public documentation.

## 2.3 (private device build)

- Repair opaque notification small icons before SystemUI tinting.
- Retain adaptive app artwork, package-update cache invalidation and profile badging.
- Verified on the original Pixel 4 XL / LineageOS device; not previously a portable public release.
