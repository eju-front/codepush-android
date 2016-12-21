package com.eju.cy.codepush;

/**
 * A {@code EjuCodePushException} gets raised whenever something goes wrong.
 *
 * @author SidneyXu
 */
public class EjuCodePushException extends Exception {

    public static final int UNKNOWN_ERROR = -1;
    public static final int MD5_NOT_MATCHED = 1000;

    private int code;

    public EjuCodePushException(String message) {
        super(message);
    }

    public EjuCodePushException(int code, String message) {
        super(message);
        this.code = code;
    }

    public EjuCodePushException(String message, Throwable cause) {
        super(message, cause);
    }

    public EjuCodePushException(Throwable cause) {
        super(cause);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EjuCodePushException{");
        sb.append("code='").append(code).append('\'');
        sb.append(", message=").append(getMessage());
        sb.append('}');
        return sb.toString();
    }
}
