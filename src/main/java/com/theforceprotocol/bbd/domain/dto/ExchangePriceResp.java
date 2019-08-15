package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Mingliang
 */
@Data
public class ExchangePriceResp {
    private Integer code;
    private String message;
    private List<ExchangePrice> data;

    @Data
    public static class ExchangePrice {
        private String timestamps;
        private String name;
        private String symbol;
        private BigDecimal price;
        private BigDecimal price_usd;
        private BigDecimal price_btc;
    }
}
