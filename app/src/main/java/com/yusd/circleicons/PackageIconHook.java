package com.yusd.circleicons;

import android.annotation.SuppressLint;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.graphics.drawable.Drawable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/** Normalize artwork before Android adds work-profile badges and shadows. */
public final class PackageIconHook {
    // LSPosed grants hidden-API access inside the scoped host process. No standalone app calls this.
    @SuppressLint({"PrivateApi", "SoonBlockedPrivateApi"})
    public static int install(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> manager = Class.forName("android.app.ApplicationPackageManager", false, loader);
        Field avatar = HookTargets.requireField(PackageItemInfo.class, "showUserIcon");
        Method unbadged = manager.getDeclaredMethod("loadUnbadgedItemIcon", PackageItemInfo.class, ApplicationInfo.class);
        XposedBridge.hookMethod(unbadged, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam call) throws IllegalAccessException {
                if (call.hasThrowable() || avatar.getInt(call.args[0]) != -10000) return;
                adaptResult(call);
            }
        });
        int count = 1;
        for (Method method : HookTargets.named(manager, "getDrawableInternal", "getDrawable")) {
            Class<?>[] args = method.getParameterTypes();
            boolean internal = Arrays.equals(args, new Class<?>[]{String.class, int.class, ApplicationInfo.class, boolean.class});
            boolean legacy = Arrays.equals(args, new Class<?>[]{String.class, int.class, ApplicationInfo.class});
            if (!internal && !legacy) continue;
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam call) {
                    if (call.hasThrowable()) return;
                    ApplicationInfo app = (ApplicationInfo) call.args[2];
                    boolean explicitIcon = internal && (boolean) call.args[3];
                    // getDrawable also serves MIME/resource artwork; only app icons belong here.
                    if (explicitIcon || (app != null && (int) call.args[1] == app.icon)) adaptResult(call);
                }
            });
            count++;
        }
        return count;
    }
    static void adaptResult(XC_MethodHook.MethodHookParam call) {
        Drawable icon = (Drawable) call.getResult();
        // PackageManager legitimately returns null for a missing resource.
        if (icon != null) call.setResult(CircleNormalizer.adapt(icon));
    }
}
