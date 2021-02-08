package io.github.ehlxr.did.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.SocketAddress;

/**
 * @author ehlxr
 */
public class NettyUtil {
    private static final Logger logger = LoggerFactory.getLogger(NettyUtil.class);

    /**
     * 获取 Channel 的远程 IP 地址
     */
    public static String parseRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }
            return addr;
        }
        return "";
    }

    public static void closeChannel(Channel channel) {
        final String addrRemote = parseRemoteAddr(channel);
        channel.close().addListener((ChannelFutureListener) future ->
                logger.info("closeChannel: close the connection to remote address[{}] result: {}",
                        addrRemote, future.isSuccess()));
    }

    public static byte[] toBytes(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        return bos.toByteArray();
    }

    public static Object toObject(byte[] bts) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bts);
        ObjectInputStream ois = new ObjectInputStream(bis);

        return ois.readObject();
    }
}
