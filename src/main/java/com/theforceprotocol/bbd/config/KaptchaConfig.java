package com.theforceprotocol.bbd.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.theforceprotocol.bbd.props.KaptchaConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KaptchaConfig {
    private final KaptchaConfigProperties configProps;

    public KaptchaConfig(KaptchaConfigProperties configProps) {
        this.configProps = configProps;
    }

    @Bean
    public Producer kaptchaProducer() {
        DefaultKaptcha result = new DefaultKaptcha();
        result.setConfig(new Config(configProps.getProps()));
        return result;
    }
}
