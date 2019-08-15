package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.entity.TokenId;

import java.math.BigDecimal;
import java.util.Map;

public interface TokenPriceService {
    Map<TokenId, BigDecimal> findErcPrices();
}
