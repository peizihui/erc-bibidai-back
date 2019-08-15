package com.theforceprotocol.bbd.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("alarm.props")
public class AlarmProperties {
    private Integer countryCode = 86;
    private String phone;
}
