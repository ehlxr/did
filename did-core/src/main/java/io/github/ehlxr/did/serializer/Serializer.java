package io.github.ehlxr.did.serializer;

import io.github.ehlxr.did.extension.SPI;

/**
 * @author ehlxr
 * @since 2021-02-08 22:12.
 */
@SPI("protostuff")
public interface Serializer {
    /**
     * 将 obj 序列化成 byte 数组
     *
     * @param obj 要序列化对象
     * @return byte 数组
     */
    <T> byte[] serializer(T obj);

    /**
     * 将 byte 数组反序列化成 class 是 clazz 的 obj 对象
     *
     * @param bytes byte 数组
     * @param clazz obj 对象 class
     * @return obj 对象
     */
    <T> T deserializer(byte[] bytes, Class<T> clazz);
}