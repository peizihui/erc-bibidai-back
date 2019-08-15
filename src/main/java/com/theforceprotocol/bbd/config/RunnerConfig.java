package com.theforceprotocol.bbd.config;

import com.theforceprotocol.bbd.domain.entity.Token;
import com.theforceprotocol.bbd.domain.entity.TokenId;
import com.theforceprotocol.bbd.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Mingliang
 */
@Slf4j
@Configuration
public class RunnerConfig implements ApplicationRunner {
    private final TokenRepository tokenRepository;

    public RunnerConfig(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, String> map = tokenRepository.findAll().stream()
                .map(Token::getId)
                .collect(toMap(TokenId::getAccount, TokenId::getSymbol));
        List<Token> list = EnumSet.allOf(Token.Tokens.class).stream()
                .filter(tokens -> !Objects.equals(tokens.name(), map.get(tokens.getAccount())))
                .map(Token.Tokens::toToken)
                .collect(toList());
        if (!list.isEmpty()) {
            List<Token> tokens = tokenRepository.saveAll(list);
            log.info("saved new tokens:{}", tokens);
        }
    }
}
