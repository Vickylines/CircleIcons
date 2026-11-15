package com.yusd.circleicons;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Each surface installs independently: ROM-specific APIs must not gate unrelated hooks. */
public final class IconHook implements IXposedHookLoadPackage {
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam app) {
        // Never hook system_server. Old resolvers run in the separate framework UI process.
        if ("android".equals(app.processName) || "system_server".equals(app.processName)) return;
        boolean frameworkUi = "system:ui".equals(app.processName);
        if (!frameworkUi && !SupportedPackages.contains(app.packageName)) return;
        HookSupport.log("loading " + app.packageName + " in " + app.processName);
        ClassLoader loader = app.classLoader;
        HookSupport.install("package icons", () -> PackageIconHook.install(loader));
        if (SupportedPackages.isLauncher(app.packageName)) {
            HookSupport.install("launcher artwork", () -> LauncherIconHook.installArtwork(loader));
            HookSupport.install("launcher cache version", () -> LauncherIconHook.installCacheVersion(loader));
        }
        if ("com.android.systemui".equals(app.packageName)) {
            HookSupport.install("splash icons", () -> SystemUiIconHook.install(loader));
            HookSupport.install("notification cache", () -> NotificationCacheHook.install(loader));
            HookSupport.install("status bar glyphs", () -> StatusBarIconHook.install(loader));
        }
        if (frameworkUi || "com.android.intentresolver".equals(app.packageName)) {
            HookSupport.install("resolver icons", () -> ResolverIconHook.install(loader));
        }
    }
}
