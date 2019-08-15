package com.theforceprotocol.bbd.config;

import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonAdvice {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonAdvice.class);
    private static final String DEFAULT_ERROR_MESSAGE = "网络不稳定，请稍后再试";

    @ExceptionHandler
    public JSONObject handlerException(Throwable e) {
        LOGGER.error("error:", e);
        if (e instanceof BusinessException) {
            BusinessException exception = (BusinessException) e;
            LOGGER.error(exception.getMessage());
            return JsonBuilder.errorBuilder(exception.getCode(), exception.getMessage());
        }
        return JsonBuilder.errorBuilder(500, DEFAULT_ERROR_MESSAGE);
    }

}
