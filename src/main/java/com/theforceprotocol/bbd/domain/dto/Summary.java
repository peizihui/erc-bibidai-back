package com.theforceprotocol.bbd.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class Summary implements Serializable {
    private static final long serialVersionUID = 1954988116939696202L;
    private Integer countryCode;
    private String phone;
    private String account;
    private String currency;
    private BigDecimal total;
    private List<ErcTokenBalance> tokens;
    private BigDecimal borrowed;
    private BigDecimal loaned;
}
