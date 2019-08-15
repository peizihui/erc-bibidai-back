package com.theforceprotocol.bbd.props;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Mingliang
 */
@Data
@Component
@ConfigurationProperties("eth.contract")
public class ContractProperties {
    private String address;
}
