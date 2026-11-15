package com.yusd.circleicons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public final class NotificationCacheHook {
    private static final String REGISTERED = "circleicons:receiver";
    public static int install(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> provider = HookTargets.findClass(loader,
                "com.android.systemui.statusbar.notification.row.icon.AppIconProviderImpl");
        if (provider == null) return 0;
        NotificationCacheAccess cache = new NotificationCacheAccess(provider);
        return XposedBridge.hookAllConstructors(provider, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam call) throws IllegalAccessException {
                if (call.hasThrowable() || XposedHelpers.getAdditionalInstanceField(call.thisObject, REGISTERED) != null) return;
                Object instance = call.thisObject;
                Context context = (Context) cache.context.get(instance);
                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_PACKAGE_ADDED);
                filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
                filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
                filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                filter.addDataScheme("package");
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override public void onReceive(Context receiverContext, Intent intent) {
                        try {
                            cache.invalidate(instance, intent.getData().getSchemeSpecificPart());
                        } catch (IllegalAccessException error) {
                            // Fields are resolved during installation; surface unexpected runtime access changes.
                            HookSupport.log("notification cache invalidation failed: " + error);
                            XposedBridge.log(error);
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
                else context.registerReceiver(receiver, filter);
                XposedHelpers.setAdditionalInstanceField(instance, REGISTERED, receiver);
            }
        }).size();
    }
}
