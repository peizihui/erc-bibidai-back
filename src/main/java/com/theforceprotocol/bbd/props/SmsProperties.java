package com.theforceprotocol.bbd.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("sms")
public class SmsProperties {
    private String url;
    private Integer userId;
    private String action;
    private String username;
    private String password;
}
