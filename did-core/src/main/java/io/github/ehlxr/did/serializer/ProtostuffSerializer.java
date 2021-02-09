/*
 * The MIT License (MIT)
 *
 * Copyright © 2020 xrv <xrg@live.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.ehlxr.did.serializer;

import io.github.ehlxr.did.common.Try;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ehlxr
 * @since 2021-02-09 11:07.
 */
public class ProtostuffSerializer implements Serializer {
    private static final Map<Class<?>, Schema<?>> CACHED_SCHEMA = new ConcurrentHashMap<>();

    @Override
    public <T> byte[] serializer(T obj) {
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

        return Try.<LinkedBuffer, byte[]>of(b -> {
            //noinspection unchecked
            Class<T> clazz = (Class<T>) obj.getClass();
            //noinspection unchecked
            Schema<T> schema = (Schema<T>) CACHED_SCHEMA.get(clazz);
            if (schema == null) {
                schema = RuntimeSchema.getSchema(clazz);
                CACHED_SCHEMA.put(clazz, schema);
            }

            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        }).apply(buffer).trap(e -> {
            throw new IllegalStateException(e.getMessage(), e);
        }).andFinally(b -> {
            ((LinkedBuffer) b).clear();
        }).get();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        return Try.<byte[], T>of(bs -> {
            Schema<T> schema = RuntimeSchema.getSchema(clazz);
            T message = schema.newMessage();

            ProtostuffIOUtil.mergeFrom(bs, message, schema);
            return message;
        }).trap(e -> {
            throw new IllegalStateException(e.getMessage(), e);
        }).apply(bytes).get();
    }
}
