package com.yusd.circleicons;

import static org.junit.Assert.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.Test;

/** Discovers actual platform hook targets; this does not claim Xposed hook execution. */
public final class PlatformInspectionTest {
    private final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    private ClassLoader packageLoader(String name) throws Exception {
        return context.createPackageContext(name, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY).getClassLoader();
    }
    @Test public void packageManagerHasUnbadgedArtworkEntry() throws Exception {
        Class<?> manager = Class.forName("android.app.ApplicationPackageManager");
        assertNotNull(manager.getDeclaredMethod("loadUnbadgedItemIcon", PackageItemInfo.class, ApplicationInfo.class));
        assertNotNull(HookTargets.requireField(PackageItemInfo.class, "showUserIcon"));
    }
    @Test public void systemUiHasNotificationGlyphEntry() throws Exception {
        ClassLoader loader = packageLoader("com.android.systemui");
        List<Method> targets = HookTargets.statusIcons(loader);
        assertEquals("An instance getIcon(StatusBarIcon) must be discoverable", 1, targets.size());
        assertNotNull(HookTargets.requireField(targets.get(0).getDeclaringClass(), "mNotification"));
        Class<?> provider = HookTargets.findClass(loader,
                "com.android.systemui.statusbar.notification.row.icon.AppIconProviderImpl");
        if (provider != null) new NotificationCacheAccess(provider);
        Log.i("CircleIconsInspection", "API " + Build.VERSION.SDK_INT + " status=" + targets + " cache=" + (provider != null));
    }
    @Test public void launcherHasArtworkEntry() throws Exception {
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> activities = context.getPackageManager().queryIntentActivities(home, 0);
        String packageName = null;
        for (ResolveInfo activity : activities) {
            if (SupportedPackages.isLauncher(activity.activityInfo.packageName)) packageName = activity.activityInfo.packageName;
        }
        assertNotNull("Install a supported Launcher3 or Pixel Launcher for this inspection", packageName);
        ClassLoader loader = packageLoader(packageName);
        List<Method> targets = HookTargets.launcherArtwork(loader);
        assertFalse("Launcher artwork normalization target", targets.isEmpty());
        List<HookTargets.CacheTarget> cache = HookTargets.launcherCache(loader);
        assertFalse("Launcher cache version target", cache.isEmpty());
        for (HookTargets.CacheTarget target : cache) Log.i("CircleIconsInspection", "cache=" + target.method + " append=" + target.append);
        Log.i("CircleIconsInspection", "API " + Build.VERSION.SDK_INT + " launcher=" + packageName + " targets=" + targets);
    }
}
