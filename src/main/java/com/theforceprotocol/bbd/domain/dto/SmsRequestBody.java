package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class SmsRequestBody {
    @NotNull(message = "the countryCode should not be null")
    private Integer countryCode;
    @NotBlank(message = "the phone should not be blank")
    private String phone;
    @NotBlank(message = "the sessionId should not be blank")
    @Pattern(regexp = "[a-z\\d]{32}", message = "the sessionId is invalid")
    private String sessionId;
    @NotBlank(message = "the captcha should not be blank")
    @Pattern(regexp = "[a-zA-Z\\d]{5}", message = "the captcha is invalid")
    private String captcha;
}
