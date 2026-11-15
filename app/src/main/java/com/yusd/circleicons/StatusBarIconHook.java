package com.yusd.circleicons;

import android.graphics.drawable.Drawable;
import android.view.View;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public final class StatusBarIconHook {
    public static int install(ClassLoader loader) throws ReflectiveOperationException {
        List<Method> targets = HookTargets.statusIcons(loader);
        for (Method method : targets) {
            Field notification = HookTargets.requireField(method.getDeclaringClass(), "mNotification");
            // Conversation avatars were added after the earliest supported platform versions.
            Field conversation = HookTargets.field(method.getDeclaringClass(), "mShowsConversation");
            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam call) throws IllegalAccessException {
                    if (call.hasThrowable() || notification.get(call.thisObject) == null
                            || (conversation != null && conversation.getBoolean(call.thisObject))) return;
                    Drawable original = (Drawable) call.getResult();
                    if (original != null) call.setResult(StatusIconNormalizer.adapt(original,
                            ((View) call.thisObject).getResources()));
                }
            });
        }
        return targets.size();
    }
}
