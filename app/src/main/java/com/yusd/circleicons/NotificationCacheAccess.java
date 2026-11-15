package com.yusd.circleicons;

import java.lang.reflect.Field;
import java.util.Map;

/** Android's newer notification artwork caches; absent on older SystemUI versions. */
final class NotificationCacheAccess {
    final Field context;
    private final Field[] appCaches;
    private final Field[] collections;
    private final Field entries;

    NotificationCacheAccess(Class<?> provider) throws ReflectiveOperationException {
        context = HookTargets.requireField(provider, "sysuiContext");
        appCaches = new Field[]{HookTargets.requireField(provider, "standardCache"),
                HookTargets.requireField(provider, "skeletonCache")};
        Class<?> cache = appCaches[0].getType();
        collections = new Field[]{HookTargets.requireField(cache, "bitmapInfoCache"),
                HookTargets.requireField(cache, "drawableCache")};
        entries = HookTargets.requireField(collections[0].getType(), "cache");
        if (!Map.class.isAssignableFrom(entries.getType())) {
            throw new NoSuchFieldException("Notification cache is no longer a Map");
        }
    }
    @SuppressWarnings("unchecked")
    void invalidate(Object provider, String packageName) throws IllegalAccessException {
        String prefix = packageName + "|";
        for (Field cache : appCaches) {
            for (Field collection : collections) {
                Map<String, ?> map = (Map<String, ?>) entries.get(collection.get(cache.get(provider)));
                map.keySet().removeIf(key -> key.startsWith(prefix));
            }
        }
    }
}
