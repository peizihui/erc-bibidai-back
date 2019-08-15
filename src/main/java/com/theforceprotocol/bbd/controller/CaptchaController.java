package com.theforceprotocol.bbd.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.theforceprotocol.bbd.service.CaptchaService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {
    private static final String IMAGE_FORMAT_NAME = "png";
    private static final String SESSION_ID = "sessionId";
    private static final String CAPTCHA_CHARS = "23456789ABCDGHJKLMNPQRSTZabcdefghjkpqrstz";
    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    private static String randomCaptcha() {
        return RandomStringUtils.random(5, CAPTCHA_CHARS.toCharArray());
    }

    @PostMapping
    public JSONObject captcha(@RequestBody Map<String, String> map) throws IOException {
        String sessionId = map.getOrDefault(SESSION_ID, "").trim();
        Assert.isTrue(sessionId.length() == 32, "invalid request");
        String captcha = randomCaptcha();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ImageIO.write(captchaService.newImage(sessionId, captcha), IMAGE_FORMAT_NAME, os);
            String data = Base64.getEncoder().encodeToString(os.toByteArray());
            return JsonBuilder.successBuilder(ImmutableMap.of("captcha", data));
        }
    }
}
