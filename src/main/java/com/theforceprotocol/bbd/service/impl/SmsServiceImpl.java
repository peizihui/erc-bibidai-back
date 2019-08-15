package com.theforceprotocol.bbd.service.impl;

import com.google.common.collect.ImmutableList;
import com.theforceprotocol.bbd.consts.SmsTemplates;
import com.theforceprotocol.bbd.domain.dto.CachedSmsCode;
import com.theforceprotocol.bbd.domain.dto.SmsRequestBody;
import com.theforceprotocol.bbd.domain.dto.SmsSendResp;
import com.theforceprotocol.bbd.domain.entity.SmsRecord;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.repository.SmsRecordRepository;
import com.theforceprotocol.bbd.service.CaptchaService;
import com.theforceprotocol.bbd.service.SmsSendService;
import com.theforceprotocol.bbd.service.SmsService;
import com.theforceprotocol.bbd.util.Errors;
import com.theforceprotocol.bbd.util.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.*;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate redisTemplate;
    private final CaptchaService captchaService;
    private final SmsSendService smsSendService;
    private final SmsRecordRepository smsRecordRepository;

    public SmsServiceImpl(StringRedisTemplate stringRedisTemplate, RedisTemplate redisTemplate, CaptchaService captchaService, SmsSendService smsSendService, SmsRecordRepository smsRecordRepository) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplate = redisTemplate;
        this.captchaService = captchaService;
        this.smsSendService = smsSendService;
        this.smsRecordRepository = smsRecordRepository;
    }

    private static String getSmsCodeKey(Integer countryCode, String phone) {
        return String.format("sms:code:%d:%s", countryCode, phone);
    }

    private static String getSmsCountKey(Integer countryCode, String phone) {
        return String.format("%s:sms:count:%d:%s", LocalDate.now(), countryCode, phone);
    }

    private static String getErrorSmsCodeKey(Integer countryCode, String phone) {
        return String.format("error:sms:code:%d:%s", countryCode, phone);
    }

    @Override
    public void sendSms(SmsRequestBody body) {
        AssertUtils.isTrue(captchaService.isValid(body.getSessionId(), body.getCaptcha()), Errors.CAPTCHA_IS_INVALID);
        Integer countryCode = body.getCountryCode();
        String phone = body.getPhone();
        preHandle(countryCode, phone);
        String code = randomNumeric(6);
        String message = String.format(SmsTemplates.VERIFICATION_CODE_TEMPLATE, code);
        SmsSendResp result = send(countryCode, phone, message);
        postHandle(countryCode, phone, code, result);
    }

    private void postHandle(Integer countryCode, String phone, String code, SmsSendResp result) {
        String smsCodeKey = getSmsCodeKey(countryCode, phone);
        if (!"ok".equals(result.getMessage())) {
            redisTemplate.delete(smsCodeKey);
            throw new IllegalArgumentException(result.getMessage());
        } else {
            String smsCountKey = getSmsCountKey(countryCode, phone);
            Long value = stringRedisTemplate.opsForValue().increment(smsCountKey, 1);
            if (value == 1L) {
                stringRedisTemplate.expire(smsCountKey, 1, DAYS);
            }
            CachedSmsCode cachedSmsCode = new CachedSmsCode(code, Instant.now());
            redisTemplate.opsForValue().set(smsCodeKey, cachedSmsCode, 5, MINUTES);
            redisTemplate.delete(getErrorSmsCodeKey(countryCode, phone));
        }
    }

    private void preHandle(Integer countryCode, String phone) {
        String smsCokeKey = getSmsCodeKey(countryCode, phone);
        CachedSmsCode cachedSmsCode = (CachedSmsCode) redisTemplate.opsForValue().get(smsCokeKey);
        AssertUtils.isTrue(
                cachedSmsCode == null || Duration.between(cachedSmsCode.getTime(), Instant.now()).toMinutes() > 1,
//                cachedSmsCode != null && Duration.between(cachedSmsCode.getTime(), Instant.now()).toMinutes() <= 1,
                Errors.SMS_HAS_SENT
        );
        String smsCountKey = getSmsCountKey(countryCode, phone);
        Long count = Optional.ofNullable(stringRedisTemplate.opsForValue().get(smsCountKey))
                .map(Long::valueOf).orElse(0L);
        log.info("smsCountKey:{}, count:{}", smsCountKey, count);
        if (count >= 15) {
            throw new BusinessException(Errors.SEND_SMS_TOO_MANY_TIMES);
        }
    }

    @Override
    public void checkSmsCode(Integer countryCode, String phone, String code) {
        String errorSmsCodeKey = getErrorSmsCodeKey(countryCode, phone);
        int currentErrorCount = Optional.ofNullable(stringRedisTemplate.opsForValue().get(errorSmsCodeKey))
                .map(Integer::parseInt).orElse(0);
        if (currentErrorCount >= 5) {
            throw new BusinessException(Errors.SMS_CODE_INVALID_TOO_MUCH);
        }
        String smsCodeKey = getSmsCodeKey(countryCode, phone);
        ValueOperations ops = redisTemplate.opsForValue();
        CachedSmsCode cachedSmsCode = (CachedSmsCode) ops.get(smsCodeKey);
        if (!Objects.equals(cachedSmsCode.getCode(), code)) {
            Long result = stringRedisTemplate.opsForValue().increment(errorSmsCodeKey);
            if (result == 1) {
                stringRedisTemplate.expire(errorSmsCodeKey, 1, HOURS);
            }
            throw new BusinessException(Errors.SMS_CODE_INVALID);
        }
        redisTemplate.delete(ImmutableList.of(smsCodeKey, errorSmsCodeKey));
    }

    @Override
    public SmsSendResp send(Integer countryCode, String phone, String message) {
        SmsSendResp result = smsSendService.send(countryCode, phone, message);
        log.info("send sms success,countryCode:{},phone:{},message:{}", countryCode, phone, message);
        SmsRecord smsRecord = new SmsRecord();
        smsRecord.setCountryCode(countryCode);
        smsRecord.setPhone(phone);
        smsRecord.setMessage(message);
        smsRecord.setInfo(result.getMessage());
        smsRecord.setStatus(result.getStatus());
        smsRecord.setRemaining(result.getRemaining());
        smsRecord.setTaskId(result.getTaskId());
        smsRecord.setSuccessCount(result.getSuccessCount());
        smsRecord.setSource("bbd");
//        SmsRecord record = smsRecordRepository.save(smsRecord);
//        log.info("save sms record:{}", record);
        return result;
    }

}
