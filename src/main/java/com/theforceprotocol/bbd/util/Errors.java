package com.theforceprotocol.bbd.util;

public enum Errors {

    LOGIN_REQUIRED(400000, ""),
    USER_LOCKED(400001, ""),
    NOT_FOUND(400004, ""),
    CAPTCHA_IS_INVALID(400015, ""),
    CAPTCHA_NEED_REFRESH(400016, ""),
    SMS_HAS_SENT(600004, ""),
    SEND_SMS_TOO_MANY_TIMES(600012, ""),
    SMS_CODE_INVALID(600003, ""),
    INVALID_PLEDGE_AMOUNT(600004, ""),
    SMS_CODE_INVALID_TOO_MUCH(600005, ""),
    INVALID_OPERATION(600006, ""),
    SAME_TOKEN(600007, ""),
    NO_TOKEN_PRICE_INFO(600008, "");

    private final Integer code;
    private final String message;

    Errors(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
