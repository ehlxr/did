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

import io.github.ehlxr.did.common.JsonUtils;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ehlxr
 */
public class SdkProto implements Serializable {
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);
    private static final long serialVersionUID = 8986093068588745760L;

    /**
     * 请求的ID
     */
    private int rqid;
    /**
     * 全局的 ID
     */
    private long did;

    public SdkProto() {
    }

    public static SdkProtoBuilder newBuilder() {
        return new SdkProtoBuilder();
    }

    public int getRqid() {
        return rqid;
    }

    public int rqid() {
        rqid = REQUEST_ID.incrementAndGet();
        return rqid;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    @Override
    public String toString() {
        return JsonUtils.obj2String(this);
    }

    public static final class SdkProtoBuilder {
        private long did;

        private SdkProtoBuilder() {
        }

        public SdkProtoBuilder did(long did) {
            this.did = did;
            return this;
        }

        public SdkProto build() {
            SdkProto sdkProto = new SdkProto();
            sdkProto.setDid(did);
            return sdkProto;
        }
    }
}
