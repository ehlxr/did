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
    private volatile static Serializer serializer = null;
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
