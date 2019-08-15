package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.domain.dto.ExchangePriceResp;
import com.theforceprotocol.bbd.domain.entity.Token.Tokens;
import com.theforceprotocol.bbd.domain.entity.TokenId;
import com.theforceprotocol.bbd.service.TokenPriceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.EnumSet.allOf;
import static java.util.stream.Collectors.toSet;

/**
 * @author Mingliang
 */
@Slf4j
@Service
public class TokenPriceServiceImpl implements TokenPriceService {
    private final WebClient blockCCWebClient;

    public TokenPriceServiceImpl(WebClient.Builder builder) {
        blockCCWebClient = builder.baseUrl("").build();
    }

    private static Map<TokenId, BigDecimal> newInitial() {
        return new HashMap<>();
    }

    @Override
    public Map<TokenId, BigDecimal> findErcPrices() {
        Set<String> pairs = allOf(Tokens.class).stream().map(Tokens::getPair).collect(toSet());
        return blockCCWebClient.get()
                .uri("?symbol_name={pair}", "binance-coin,dai,forceprotocol,huobi-token,loopring,0x")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(ExchangePriceResp.class)
                .filter(r -> r.getCode() == 0)
                .flatMapIterable(ExchangePriceResp::getData)
                .filter(price -> pairs.contains(price.getName()))
                .retry(3)
                .timeout(Duration.ofMinutes(1))
                .reduce(newInitial(), (m, tp) -> {
                    m.put(new TokenId(Tokens.valueOf(tp.getSymbol()).getAccount(), tp.getSymbol()), tp.getPrice_usd());
                    return m;
                }).blockOptional(Duration.ofMinutes(1))
                .get();
    }
}