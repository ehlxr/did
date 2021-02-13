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

package io.github.ehlxr.did;

import io.github.ehlxr.did.extension.ExtensionLoader;
import io.github.ehlxr.did.serializer.Serializer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ehlxr
 * @since 2021-02-13 11:59.
 */
public class SerializerTest {
    private static final Logger logger = LoggerFactory.getLogger(SerializerTest.class);
    private SdkProto message;

    @Before
    public void init() {
        message = SdkProto.newBuilder().did(238231232112121L).build();
        message.rqid();
    }

    @Test
    public void jdkTest() {
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("jdk");

        long b1 = System.currentTimeMillis();
        byte[] bytes = serializer.serializer(message);
        long e1 = System.currentTimeMillis();
        logger.info("jdk serialize result length = {}, cost time：{}", bytes.length, (e1 - b1));

        long b2 = System.currentTimeMillis();
        SdkProto data = serializer.deserializer(bytes, SdkProto.class);
        long e2 = System.currentTimeMillis();
        logger.info("jdk deserialize result：{}, cost time：{}", data, (e2 - b2));
    }

    @Test
    public void hessianTest() {
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("hessian");

        long b1 = System.currentTimeMillis();
        byte[] bytes = serializer.serializer(message);
        long e1 = System.currentTimeMillis();
        logger.info("hessian serialize result length = {}, cost time：{}", bytes.length, (e1 - b1));

        long b2 = System.currentTimeMillis();
        SdkProto data = serializer.deserializer(bytes, SdkProto.class);
        long e2 = System.currentTimeMillis();
        logger.info("hessian deserialize result：{}, cost time：{}", data, (e2 - b2));
    }

    @Test
    public void protostuffTest() {
        Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension("protostuff");

        long b1 = System.currentTimeMillis();
        byte[] bytes = serializer.serializer(message);
        long e1 = System.currentTimeMillis();
        logger.info("protostuff serialize result length = {}, cost time：{}", bytes.length, (e1 - b1));

        long b2 = System.currentTimeMillis();
        SdkProto data = serializer.deserializer(bytes, SdkProto.class);
        long e2 = System.currentTimeMillis();
        logger.info("protostuff deserialize result：{}, cost time：{}", data, (e2 - b2));
    }
}
