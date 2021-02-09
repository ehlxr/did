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
