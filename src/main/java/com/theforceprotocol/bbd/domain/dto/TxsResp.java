package com.theforceprotocol.bbd.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
public class TxsResp {
    private BigDecimal sum;
    private Page<Tx> page;

    @Data
    public static class Tx {
        private String orderNumber;
        private BigDecimal amount;
        private Instant ts;
    }

}
