package com.theforceprotocol.bbd.exception;

import com.theforceprotocol.bbd.util.Errors;

public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = -6145243340861513085L;
    private final int code;

    public BusinessException(Errors error) {
        super(error.getMessage());
        code = error.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
