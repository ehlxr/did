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

import io.github.ehlxr.did.common.Constants;
import io.github.ehlxr.did.extension.ExtensionLoader;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author ehlxr
 * @since 2021-02-08 22:12.
 */
public final class SerializerHolder {
    volatile private static Serializer serializer = null;
    private static final Logger logger = LoggerFactory.getLogger(SerializerHolder.class);

    private SerializerHolder() {
    }

    public static Serializer get() {
        if (serializer == null) {
            synchronized (SerializerHolder.class) {
                if (serializer == null) {
                    String serializerName = Constants.getEnv("DID_SERIALIZER");
                    if (!StringUtil.isNullOrEmpty(serializerName)) {
                        serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
                    }
                    serializer = Objects.isNull(serializer) ?
                            ExtensionLoader.getExtensionLoader(Serializer.class).getDefaultExtension() :
                            serializer;

                    logger.debug("loaded {} serializer", serializer.getClass().getName());
                }
            }
        }
        return serializer;
    }
}
