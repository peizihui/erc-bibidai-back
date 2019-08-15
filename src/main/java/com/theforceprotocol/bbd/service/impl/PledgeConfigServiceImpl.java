package com.theforceprotocol.bbd.service.impl;

import com.google.common.collect.ImmutableList;
import com.theforceprotocol.bbd.domain.dto.PledgeConfigs;
import com.theforceprotocol.bbd.domain.entity.Token;
import com.theforceprotocol.bbd.domain.entity.Token.TokenType;
import com.theforceprotocol.bbd.props.PledgeProperties;
import com.theforceprotocol.bbd.repository.ErcTokenRepository;
import com.theforceprotocol.bbd.repository.TokenRepository;
import com.theforceprotocol.bbd.service.CacheService;
import com.theforceprotocol.bbd.service.PledgeConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static com.theforceprotocol.bbd.domain.dto.PledgeConfigs.EosToken;
import static com.theforceprotocol.bbd.domain.dto.PledgeConfigs.PledgeTokens;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Mingliang
 */
@Slf4j
@Service
public class PledgeConfigServiceImpl implements PledgeConfigService {
    private final TokenRepository tokenRepository;
    private final PledgeProperties props;
    private final CacheService cacheService;

    public PledgeConfigServiceImpl(TokenRepository tokenRepository, ErcTokenRepository ercTokenRepository, PledgeProperties props, CacheService cacheService) {
        this.tokenRepository = tokenRepository;
        this.props = props;
        this.cacheService = cacheService;
    }

    private static List<EosToken> from(List<Token> list) {
        return list.stream().map(EosToken::from).collect(toList());
    }

    @Override
    public PledgeConfigs findConfigs() {
        String key = "pledge:common:configs";
        return cacheService.load(key, 5, MINUTES, PledgeConfigs.class, () -> {
            Map<TokenType, List<Token>> map = tokenRepository.findAll().stream()
                    .filter(Token::isEnabled)
                    .collect(groupingBy(Token::getType));
            PledgeConfigs configs = new PledgeConfigs();
            PledgeTokens tokens = new PledgeTokens();
            List<Token> allTypeTokens = map.get(TokenType.ALL);
            List<Token> pledgeTokens = map.get(TokenType.PLEDGE);
            List<Token> borrowedTokens = map.get(TokenType.BORROWED);
            tokens.setPledge(from(ImmutableList.<Token>builder().addAll(allTypeTokens).addAll(pledgeTokens).build()));
            tokens.setBorrowed(from(ImmutableList.<Token>builder().addAll(allTypeTokens).addAll(borrowedTokens).build()));
            configs.setTokens(tokens);
            configs.setPeriod(props.getPeriod());
            configs.setPeriodUnit(props.getPeriodUnit());
            configs.setFeeRate(props.getFeeRate());
            return configs;
        });
    }
}
