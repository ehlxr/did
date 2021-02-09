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

package io.github.ehlxr.did.adapter;

import io.github.ehlxr.did.common.Try;
import io.github.ehlxr.did.serializer.SerializerHolder;

import java.io.Serializable;

/**
 * @author ehlxr
 * @since 2021-02-08 22:07.
 */
public class Message<T> implements Serializable {
    private static final long serialVersionUID = 2419320297359425651L;

    /**
     * 类型（例如：0xA 表示 A 系统；0xB 表示 B 系统）
     */
    private byte type;

    /**
     * 信息标志（例如：0xA 表示心跳包；0xB 表示超时包；0xC 业务信息包）
     */
    private byte flag;

    /**
     * 内容长度（通过 content 计算而来）
     */
    private int length;

    /**
     * 消息内容
     */
    private T content;

    public Message(byte type, byte flag, T content) {
        this.type = type;
        this.flag = flag;
        this.content = content;
    }

    public Message() {
    }

    public static <T> MessageBuilder<T> newBuilder() {
        return new MessageBuilder<>();
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getFlag() {
        return flag;
    }

    public void setFlag(byte flag) {
        this.flag = flag;
    }

    public int getLength() {
        return getContent().length;
    }

    public byte[] getContent() {
        return Try.<T, byte[]>of(SerializerHolder.get()::serializer).apply(content).get();
    }

    public void setContent(T content) {
        this.content = content;
    }

    public T content(Class<T> clazz) {
        return SerializerHolder.get().deserializer(getContent(), clazz);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", flag=" + flag +
                ", length=" + length +
                ", content=" + content +
                '}';
    }

    public static final class MessageBuilder<T> {
        private byte type;
        private byte flag;
        private T content;

        private MessageBuilder() {
        }

        public MessageBuilder<T> type(byte type) {
            this.type = type;
            return this;
        }

        public MessageBuilder<T> flag(byte flag) {
            this.flag = flag;
            return this;
        }

        public MessageBuilder<T> content(T content) {
            this.content = content;
            return this;
        }

        public Message<T> build() {
            Message<T> message = new Message<>();
            message.setType(type);
            message.setFlag(flag);
            message.setContent(content);
            return message;
        }
    }
}