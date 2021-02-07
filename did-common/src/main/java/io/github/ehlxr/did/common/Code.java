package io.github.ehlxr.did.common;

/**
 * 自定义状态码
 *
 * @author ehlxr
 * @since 2020/3/18.
 */
public enum Code {
    /**
     * 成功
     */
    SUCCESSFUL(200, "success"),
    /**
     * 未知异常
     */
    UNKNOWN_EXCEPTION(600, "系统异常，请联系管理员");

    private static final Code[] CODES = Code.values();
    private final int code;
    private final String message;

    Code(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static Code code(int code) {
        for (Code c : CODES) {
            if (code == c.getCode()) {
                return c;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Code{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }

}

