package cn.ceres.did.server.sdk;

/**
 * @author ehlxr
 */
public class SdkProto {
    /**
     * 请求的ID
     */
    private int rqid;
    /**
     * 全局的 ID
     */
    private long did;

    SdkProto(int rqid, long did) {
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
