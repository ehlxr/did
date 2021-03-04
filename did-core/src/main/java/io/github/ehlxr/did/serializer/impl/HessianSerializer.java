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

package io.github.ehlxr.did.serializer.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import io.github.ehlxr.did.common.Try;
import io.github.ehlxr.did.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author ehlxr
 * @since 2021-02-09 11:07.
 */
public class HessianSerializer implements Serializer {
    @Override
    public <T> byte[] serializer(T obj) {
        return Try.<T, byte[]>of(o -> {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            HessianOutput ho = new HessianOutput(bos);
            ho.writeObject(obj);
            ho.flush();

            return bos.toByteArray();
        }).trap(Throwable::printStackTrace).apply(obj).get();
    }

    @Override
    public <T> T deserializer(byte[] bytes, Class<T> clazz) {
        return Try.<byte[], T>of(bs -> {
            ByteArrayInputStream bis = new ByteArrayInputStream(bs);
            HessianInput hi = new HessianInput(bis);

            Object o = hi.readObject();
            return clazz.isInstance(o) ? clazz.cast(o) : null;
        }).trap(Throwable::printStackTrace).apply(bytes).get();
    }
}
