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

package io.github.ehlxr.did.netty;

import io.github.ehlxr.did.common.NettyUtil;
import io.github.ehlxr.did.common.Try;

/**
 * @author ehlxr
 * @since 2021-02-08 22:07.
 */
public class MyProtocolBean {
    // 类型（系统编号 0xA 表示A系统，0xB 表示B系统）
    private byte type;

    // 信息标志  0xA 表示心跳包 0xB 表示超时包  0xC 业务信息包
    private byte flag;

    // 内容长度
    private int length;

    // 内容
    private byte[] content;

    public MyProtocolBean(byte type, byte flag, int length, byte[] content) {
        this.type = type;
        this.flag = flag;
        this.length = length;
        this.content = content;
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
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MyProtocolBean{" +
                "type=" + type +
                ", flag=" + flag +
                ", length=" + length +
                ", content=" + Try.of(NettyUtil::toObject).apply(content).get() +
                '}';
    }
}