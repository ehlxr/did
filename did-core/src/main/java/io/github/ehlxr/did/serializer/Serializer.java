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