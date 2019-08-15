package com.theforceprotocol.bbd.props;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties("pledge.config")
public class PledgeProperties {
    private BigDecimal feeRate = new BigDecimal("");
    private List<Integer> period = ImmutableList.of(1);
    private TimeUnit periodUnit = TimeUnit.DAYS;
    private Rates pledgeRates = new Rates(new BigDecimal(""), new BigDecimal(""), new BigDecimal(""));
    private String banner = "";

    @Data
    @AllArgsConstructor
    public static class Rates {
        BigDecimal minRate;
        BigDecimal alarmRate;
        BigDecimal maxRate;
    }

}
