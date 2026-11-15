package com.yusd.circleicons;

import android.graphics.drawable.Drawable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared by production hooks and inspection tests against real system APKs. */
final class HookTargets {
    static Class<?> findClass(ClassLoader loader, String name) {
        try {
            return Class.forName(name, false, loader);
        } catch (ClassNotFoundException absent) {
            return null;
        }
    }
    static Field field(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field result = current.getDeclaredField(name);
                result.setAccessible(true);
                return result;
            } catch (NoSuchFieldException absent) {
                // Private fields can live in the platform base implementation.
            }
        }
        return null;
    }
    static Field requireField(Class<?> type, String name) throws NoSuchFieldException {
        Field result = field(type, name);
        if (result == null) throw new NoSuchFieldException(type.getName() + "." + name);
        return result;
    }
    static List<Method> named(Class<?> type, String... names) {
        Map<String, Method> result = new LinkedHashMap<>();
        List<String> accepted = Arrays.asList(names);
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!accepted.contains(method.getName()) || Modifier.isAbstract(method.getModifiers())) continue;
                String signature = method.getName() + Arrays.toString(method.getParameterTypes());
                result.putIfAbsent(signature, method);
            }
        }
        return new ArrayList<>(result.values());
    }
    static List<Method> drawableFirst(Class<?> type, String... names) {
        List<Method> result = new ArrayList<>();
        for (Method method : named(type, names)) {
            Class<?>[] args = method.getParameterTypes();
            if (args.length > 0 && args[0] == Drawable.class) result.add(method);
        }
        return result;
    }
    static List<Method> statusIcons(ClassLoader loader) {
        Class<?> type = findClass(loader, "com.android.systemui.statusbar.StatusBarIconView");
        List<Method> result = new ArrayList<>();
        for (Method method : named(type, "getIcon")) {
            Class<?>[] args = method.getParameterTypes();
            if (!Modifier.isStatic(method.getModifiers()) && args.length == 1
                    && args[0].getName().equals("com.android.internal.statusbar.StatusBarIcon")
                    && Drawable.class.isAssignableFrom(method.getReturnType())) result.add(method);
        }
        return result;
    }
    static List<Method> launcherArtwork(ClassLoader loader) {
        Class<?> factory = findClass(loader, "com.android.launcher3.icons.BaseIconFactory");
        List<Method> result = drawableFirst(factory, "wrapToAdaptiveIcon", "normalizeAndWrapToAdaptiveIcon");
        if (!result.isEmpty()) return result;
        // Launcher3 on Oreo/Pie predates iconloaderlib's BaseIconFactory.
        return drawableFirst(findClass(loader, "com.android.launcher3.graphics.LauncherIcons"),
                "createBadgedIconBitmap");
    }

    static final class CacheTarget {
        final Method method;
        final Method append;
        CacheTarget(Method method, Method append) { this.method = method; this.append = append; }
    }

    static List<CacheTarget> launcherCache(ClassLoader loader) {
        List<CacheTarget> result = new ArrayList<>();
        Class<?> provider = findClass(loader, "com.android.launcher3.icons.LauncherIconProviderImpl");
        for (Method method : named(provider, "getApplicationInfoHash")) {
            for (Method append : named(method.getReturnType(), "withAdditionalValues")) {
                if (Arrays.equals(append.getParameterTypes(), new Class<?>[]{String[].class})) {
                    result.add(new CacheTarget(method, append));
                }
            }
        }
        if (!result.isEmpty()) return result;
        for (String name : new String[]{"com.android.launcher3.icons.IconProvider", "com.android.launcher3.IconProvider"}) {
            for (Method method : named(findClass(loader, name), "getSystemStateForPackage", "getIconSystemState")) {
                if (method.getReturnType() == String.class) result.add(new CacheTarget(method, null));
            }
        }
        return result;
    }
}
