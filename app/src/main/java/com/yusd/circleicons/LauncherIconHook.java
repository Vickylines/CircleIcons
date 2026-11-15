package com.yusd.circleicons;

import android.graphics.drawable.Drawable;
import java.lang.reflect.Method;
import java.util.List;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

final class LauncherIconHook {
    private static final String CACHE_TAG = "|circleicons:" + BuildConfig.VERSION_NAME;
    static int installArtwork(ClassLoader loader) {
        List<Method> targets = HookTargets.launcherArtwork(loader);
        for (Method method : targets) {
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam call) {
                    Drawable icon = (Drawable) call.args[0];
                    if (icon != null) call.args[0] = CircleNormalizer.adapt(icon);
                }
            });
        }
        return targets.size();
    }
    static int installCacheVersion(ClassLoader loader) {
        List<HookTargets.CacheTarget> targets = HookTargets.launcherCache(loader);
        for (HookTargets.CacheTarget target : targets) {
            if (target.append != null) target.append.setAccessible(true);
            XposedBridge.hookMethod(target.method, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam call) throws ReflectiveOperationException {
                    if (call.hasThrowable()) return;
                    if (target.append != null) {
                        call.setResult(target.append.invoke(call.getResult(), (Object) new String[]{CACHE_TAG}));
                    } else {
                        String state = (String) call.getResult();
                        if (state != null && !state.endsWith(CACHE_TAG)) call.setResult(state + CACHE_TAG);
                    }
                }
            });
        }
        return targets.size();
    }
}
