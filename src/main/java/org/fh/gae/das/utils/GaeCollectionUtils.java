package org.fh.gae.das.utils;

import java.util.Map;
import java.util.function.Supplier;

public class GaeCollectionUtils {
    private GaeCollectionUtils() {

    }

    /**
     * 从Map中按key取值, 如果不存在则创建,入map, 并返回创建的对象
     * @param key
     * @param map
     * @param factory
     * @return
     */
    public static <T, R> R getAndCreateIfNeed(T key, Map<T, R> map, Supplier<R> factory) {
        R ret = map.get(key);
        if (null == ret) {
            ret = factory.get();
            map.put(key, ret);
        }

        return ret;
    }
}
