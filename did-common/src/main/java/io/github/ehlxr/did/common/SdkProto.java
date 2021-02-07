package io.github.ehlxr.did.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ehlxr
 */
public class SdkProto {
    private static final AtomicInteger REQUEST_ID = new AtomicInteger(0);

    /**
     * 请求的ID
     */
    private int rqid;
    /**
     * 全局的 ID
     */
    private long did;

    public SdkProto() {
        rqid = REQUEST_ID.incrementAndGet();
        did = 0;
    }

    public SdkProto(int rqid, long did) {
        this.rqid = rqid;
        this.did = did;
    }

    public static SdkProtoBuilder newBuilder() {
        return new SdkProtoBuilder();
    }

    public int getRqid() {
        return rqid;
    }

    public void setRqid(int rqid) {
        if (rqid > 0) {
            this.rqid = rqid;
        }
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
        private int rqid;
        private long did;

        private SdkProtoBuilder() {
        }

        public SdkProtoBuilder rqid(int rqid) {
            this.rqid = rqid;
            return this;
        }

        public SdkProtoBuilder did(long did) {
            this.did = did;
            return this;
        }

        public SdkProto build() {
            SdkProto sdkProto = new SdkProto();
            sdkProto.setRqid(rqid);
            sdkProto.setDid(did);
            return sdkProto;
        }
    }
}
