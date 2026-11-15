package com.yusd.circleicons;

import android.graphics.drawable.Drawable;
import java.lang.reflect.Method;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public final class ResolverIconHook {
    public static int install(ClassLoader loader) {
        int count = 0;
        for (String name : new String[]{"com.android.intentresolver.SimpleIconFactory", "com.android.internal.app.SimpleIconFactory"}) {
            for (Method method : HookTargets.drawableFirst(HookTargets.findClass(loader, name), "createUserBadgedIconBitmap")) {
                XposedBridge.hookMethod(method, new XC_MethodHook() {
                    @Override protected void beforeHookedMethod(MethodHookParam call) {
                        Drawable icon = (Drawable) call.args[0];
                        if (icon != null) call.args[0] = CircleNormalizer.adapt(icon);
                    }
                });
                count++;
            }
        }
        return count;
    }
}
