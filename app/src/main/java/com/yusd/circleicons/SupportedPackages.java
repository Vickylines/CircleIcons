package com.yusd.circleicons;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

final class SupportedPackages {
    private static final Set<String> PACKAGES = new HashSet<>(Arrays.asList(
            "com.android.launcher3", "com.google.android.apps.nexuslauncher",
            "com.android.systemui", "com.android.settings", "com.android.settings.intelligence",
            "com.google.android.settings.intelligence", "com.android.permissioncontroller",
            "com.google.android.permissioncontroller", "com.android.packageinstaller",
            "com.google.android.packageinstaller", "com.android.intentresolver",
            "com.android.documentsui", "com.google.android.documentsui", "org.lineageos.lineageparts"));

    static boolean contains(String packageName) { return PACKAGES.contains(packageName); }
    static boolean isLauncher(String packageName) {
        return "com.android.launcher3".equals(packageName)
                || "com.google.android.apps.nexuslauncher".equals(packageName);
    }
}
