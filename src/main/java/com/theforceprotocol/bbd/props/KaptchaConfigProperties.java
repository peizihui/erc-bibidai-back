package com.theforceprotocol.bbd.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Data
@Component
@ConfigurationProperties("kaptcha.config")
public class KaptchaConfigProperties {
    private Properties props;
}
