package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class LoginOrRegisterBody {
    @NotNull(message = "the countryCode should not be null")
    private Integer countryCode;
    @NotBlank(message = "the phone should not be blank")
    private String phone;
    @NotBlank(message = "the code should not be blank")
    @Pattern(regexp = "\\d{6}", message = "the code is invalid")
    private String code;
}
