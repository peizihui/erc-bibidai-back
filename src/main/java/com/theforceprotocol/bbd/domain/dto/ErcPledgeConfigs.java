package com.theforceprotocol.bbd.domain.dto;

import com.theforceprotocol.bbd.domain.entity.ErcToken;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Mingliang
 */
@Data
public class ErcPledgeConfigs {
    private ErcTokens tokens;
    private List<Integer> period;
    private TimeUnit periodUnit;
    private BigDecimal feeRate;

    @Data
    public static class ErcTokens implements Serializable {
        private static final long serialVersionUID = 662587732732729992L;
        private List<ErcToken> pledge;
        private List<ErcToken> borrowed;
    }
}
