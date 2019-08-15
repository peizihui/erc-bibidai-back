package com.theforceprotocol.bbd.domain.dto;

import com.theforceprotocol.bbd.domain.entity.Token;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
public class PledgeConfigs implements Serializable {
    private static final long serialVersionUID = 224264110809930575L;
    private PledgeTokens tokens;
    private List<Integer> period;
    private TimeUnit periodUnit;
    private BigDecimal feeRate;


    @Data
    public static class PledgeTokens implements Serializable {
        private static final long serialVersionUID = 5810992935715173097L;
        private List<EosToken> pledge;
        private List<EosToken> borrowed;
    }

    @Data
    public static class EosToken implements Serializable {
        private static final long serialVersionUID = 7899580710079136684L;
        private String account;
        private String symbol;
        private Integer decimals;
        private String logoUrl;

        public static EosToken from(Token token) {
            EosToken eosToken = new EosToken();
            eosToken.setAccount(token.getId().getAccount());
            eosToken.setSymbol(token.getId().getSymbol());
            eosToken.setDecimals(token.getDecimals());
            eosToken.setLogoUrl(token.getLogoUrl());
            return eosToken;
        }
    }
}
