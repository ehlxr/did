package io.github.ehlxr.did.serializer;

import io.github.ehlxr.did.extension.ExtensionLoader;

/**
 * @author ehlxr
 * @since 2021-02-08 22:12.
 */
public final class SerializerHolder {
    volatile private static Serializer serializer = null;

    private SerializerHolder() {
    }

    public static Serializer get() {
        if (serializer == null) {
            synchronized (SerializerHolder.class) {
                if (serializer == null) {
                    serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getDefaultExtension();
                }
            }
        }
        return serializer;
    }
}
