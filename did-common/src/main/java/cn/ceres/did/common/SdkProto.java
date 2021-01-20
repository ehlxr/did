package cn.ceres.did.common;

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

    public int getRqid() {
        return rqid;
    }

    public void setRqid(int rqid) {
        this.rqid = rqid;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    @Override
    public String toString() {
        return "SdkProto{" +
                "rqid=" + rqid +
                ", did=" + did +
                '}';
    }
}
