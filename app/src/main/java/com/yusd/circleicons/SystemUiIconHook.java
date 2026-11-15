package com.yusd.circleicons;

import android.content.pm.PackageItemInfo;
import android.graphics.drawable.Drawable;
import java.lang.reflect.Method;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/** Android 12+ shell splash artwork can bypass the normal PackageManager entry point. */
public final class SystemUiIconHook {
    public static int install(ClassLoader loader) {
        Class<?> provider = HookTargets.findClass(loader, "com.android.launcher3.icons.IconProvider");
        int count = 0;
        for (Method method : HookTargets.named(provider, "getIcon")) {
            Class<?>[] args = method.getParameterTypes();
            if (args.length == 0 || !PackageItemInfo.class.isAssignableFrom(args[0])
                    || !Drawable.class.isAssignableFrom(method.getReturnType())) continue;
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam call) {
                    if (!call.hasThrowable()) PackageIconHook.adaptResult(call);
                }
            });
            count++;
        }
        return count;
    }
}
