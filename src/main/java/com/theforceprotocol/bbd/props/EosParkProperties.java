package com.theforceprotocol.bbd.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("eos.park")
public class EosParkProperties {
    private List<String> apiKeys;
}
