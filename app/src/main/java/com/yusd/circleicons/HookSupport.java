package com.yusd.circleicons;

import de.robv.android.xposed.XposedBridge;

final class HookSupport {
    interface Installation { int run() throws ReflectiveOperationException; }
    static void install(String feature, Installation installation) {
        try {
            int count = installation.run();
            log(feature + (count == 0 ? ": unavailable on this ROM" : ": active (" + count + ")"));
        } catch (ReflectiveOperationException | LinkageError | RuntimeException error) {
            // External ROM/API boundary: report the failure and continue other features.
            log(feature + ": installation failed: " + error);
            XposedBridge.log(error);
        }
    }
    static void log(String message) {
        XposedBridge.log("CircleIcons " + BuildConfig.VERSION_NAME + ": " + message);
    }
}
