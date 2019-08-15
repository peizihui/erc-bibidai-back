package com.theforceprotocol.bbd.service.impl;

import com.google.code.kaptcha.Producer;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.service.CaptchaService;
import com.theforceprotocol.bbd.util.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.image.RenderedImage;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {
    private static final String CAPTCHA_KEY = "";
    private final StringRedisTemplate stringRedisTemplate;
    private final Producer producer;

    public CaptchaServiceImpl(StringRedisTemplate stringRedisTemplate, Producer producer) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.producer = producer;
    }

    private static String getCaptchaKey(String sessionId) {
        return String.format("%s:%s", CAPTCHA_KEY, sessionId);
    }

    @Override
    public RenderedImage newImage(String sessionId, String text) {
        String key = getCaptchaKey(sessionId);
        stringRedisTemplate.opsForValue().set(key, text, 3, TimeUnit.MINUTES);
        log.info("set key:{},value:{},timeout:3 min", key, text);
        return producer.createImage(text);
    }

    @Override
    public Boolean isValid(String sessionId, String text) {
        String key = getCaptchaKey(sessionId);
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new BusinessException(Errors.CAPTCHA_NEED_REFRESH);
        }
        Boolean result = value.equalsIgnoreCase(text);
        stringRedisTemplate.delete(key);
        log.info("delete key:{}", key);
        return result;
    }

}
