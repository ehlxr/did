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

package io.github.ehlxr.did.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.MissingNode;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * JSON 处理类
 *
 * @author ehlxr
 * @since 2020/5/6.
 */
@SuppressWarnings({"unused", "unchecked"})
public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    static {
        // 对象的所有字段全部列入
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 取消默认转换 timestamps 形式
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        // 忽略空 bean 转 JSON 的错误
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        // 所有的日期格式都统一为以下的样式：yyyy-MM-dd HH:mm:ss
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 忽略在 JSON 字符串中存在，但是在 java 对象中不存在对应属性的情况
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper om() {
        return OBJECT_MAPPER;
    }

    /**
     * 对象转为 JsonNode 实例
     *
     * @param obj 要转换的对象
     * @param <T> 要转换的对象类型
     * @return {@link JsonNode}实例
     */
    public static <T> JsonNode obj2JsonNode(T obj) {
        try {
            return OBJECT_MAPPER.readTree(obj2String(obj));
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return MissingNode.getInstance();
        }
    }

    /**
     * 对象转为 JSON 字符串
     *
     * @param obj 要转换的对象
     * @param <T> 要转换的对象类型
     * @return JSON 字符串
     */
    public static <T> String obj2String(T obj) {
        if (obj == null) {
            return "";
        }
        try {
            return obj instanceof String ? (String) obj : OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return "";
        }
    }

    /**
     * 对象转为格式化的 JSON 字符串
     *
     * @param obj 要转换的对象
     * @param <T> 要转换的对象类型
     * @return 格式化的 JSON 字符串
     */
    public static <T> String obj2StringPretty(T obj) {
        if (obj == null) {
            return "";
        }
        try {
            return obj instanceof String ?
                    (String) obj :
                    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return "";
        }
    }

    /**
     * 字符串转换为自定义对象
     *
     * @param str   要转换的字符串
     * @param clazz 自定义对象的 class 对象
     * @param <T>   自定义对象类型
     * @return 自定义对象
     */
    public static <T> T string2Obj(String str, Class<T> clazz) {
        if (StringUtil.isNullOrEmpty(str) || clazz == null) {
            throw new RuntimeException("json string to obj param should not empty");
        }
        try {
            return clazz.equals(String.class) ? (T) str : OBJECT_MAPPER.readValue(str, clazz);
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * 字符串转换为自定义对象
     *
     * @param str           要转换的字符串
     * @param typeReference 集合对象 typeReference
     * @param <T>           集合对象类型
     * @return 自定义对象
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference) {
        if (StringUtil.isNullOrEmpty(str) || typeReference == null) {
            throw new RuntimeException("json string to obj param should not empty");
        }
        try {
            return typeReference.getType().equals(String.class) ?
                    (T) str :
                    OBJECT_MAPPER.readValue(str, typeReference);
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return null;
        }
    }

    /**
     * 字符串转换为自定义对象
     *
     * @param str             要转换的字符串
     * @param collectionClazz 集合 class
     * @param elementClazzes  集合对象 class
     * @param <T>             集合对象类型
     * @return 自定义对象
     */
    public static <T> T string2Obj(String str, Class<?> collectionClazz, Class<?>... elementClazzes) {
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(collectionClazz, elementClazzes);
        try {
            return OBJECT_MAPPER.readValue(str, javaType);
        } catch (JsonProcessingException e) {
            logger.error("", e);
            return null;
        }
    }
}

