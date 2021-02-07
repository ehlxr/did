package io.github.ehlxr.did.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.netty.util.internal.StringUtil;

import java.io.Serializable;
import java.util.Objects;

/**
 * 统一输出结果集
 *
 * @author ehlxr
 * @since 2020/3/18.
 */
@SuppressWarnings("unused")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -2758720512348727698L;
    /**
     * 响应编码
     */
    private int code;
    /**
     * 消息，如错误消息
     */
    private String message;
    /**
     * 数据内容
     */
    private T data;

    private Result() {
    }

    private Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> Result<T> success(T data, String message) {
        return new Result<>(Code.SUCCESSFUL.getCode(), data, message);
    }

    public static <T> Result<T> success(T data) {
        return success(data, null);
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> of(int c, T d, String m) {
        return new Result<>(c, d, m);
    }

    public static <T> Result<T> of(Code c, T d, String m) {
        return new Result<>(c.getCode(), d, m);
    }

    public static <T> Result<T> fail(Code code, String message) {
        return of(code.getCode(), null, message);
    }

    public static <T> Result<T> fail(String message) {
        return fail(Code.UNKNOWN_EXCEPTION.getCode(), message);
    }

    public static <T> Result<T> fail(int code, String message) {
        return of(code, null, message);
    }

    public static <T> Result<T> fail(Code code) {
        return fail(code, code.getMessage());
    }

    public static <T> Result<T> fail(Throwable e) {
        return of(Code.UNKNOWN_EXCEPTION.getCode(), null, String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage()));
    }

    public String getMessage() {
        // return StringUtil.isNullOrEmpty(m) ? c.getMessage() : m;
        if (StringUtil.isNullOrEmpty(message)) {
            Code code;
            try {
                code = Code.code(this.code);
            } catch (Exception e) {
                return message;
            }

            return Objects.isNull(code) ? "" : code.getMessage();
        }
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        // return Objects.nonNull(c) ? c.getCode() : Code.UNKNOWN_EXCEPTION.getCode();
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Result<?> result = (Result<?>) o;
        return code == result.code &&
                Objects.equals(message, result.message) &&
                Objects.equals(data, result.data);
    }

    @Override
    public String toString() {
        return JsonUtils.obj2String(this);
    }
}

