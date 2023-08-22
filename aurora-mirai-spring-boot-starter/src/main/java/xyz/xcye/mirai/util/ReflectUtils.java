package xyz.xcye.mirai.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author xcye
 * @description
 * @date 2023-08-21 14:39:12
 */

public class ReflectUtils {

    /**
     * 根据index获取接口的泛型类型，index代表的是MiraiListenable<T, F, V>第几个泛型
     *
     * @param obj   对象
     * @param index 泛型下标
     * @return 类型
     */
    public static Class<?> getInterfaceGenericType(Object obj, int index) {
        Type[] types = obj.getClass().getGenericInterfaces();
        if (types.length == 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        ParameterizedType parameterizedType = (ParameterizedType) types[0];
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (index >= actualTypeArguments.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        return actualTypeArguments[index].getClass();
    }
}