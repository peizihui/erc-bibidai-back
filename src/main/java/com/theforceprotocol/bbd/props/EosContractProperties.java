package com.theforceprotocol.bbd.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@ConfigurationProperties("eos.contract")
public class EosContractProperties {
    private String account;
    private String pwd;
}
