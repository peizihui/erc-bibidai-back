package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.domain.dto.ErcTokenBalance;
import com.theforceprotocol.bbd.domain.dto.LoginOrRegisterBody;
import com.theforceprotocol.bbd.domain.dto.Summary;
import com.theforceprotocol.bbd.domain.entity.Token;
import com.theforceprotocol.bbd.domain.entity.TokenId;
import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.repository.OrderRepository;
import com.theforceprotocol.bbd.repository.TokenRepository;
import com.theforceprotocol.bbd.repository.UserRepository;
import com.theforceprotocol.bbd.service.CacheService;
import com.theforceprotocol.bbd.service.SmsService;
import com.theforceprotocol.bbd.service.TokenPriceService;
import com.theforceprotocol.bbd.service.UserService;
import com.theforceprotocol.bbd.util.Errors;
import com.theforceprotocol.bbd.domain.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.theforceprotocol.bbd.util.DataUtils.hideWithStar;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final SmsService smsService;
    private final TokenPriceService tokenPriceService;
    private final TokenRepository tokenRepository;
    private final OrderRepository orderRepository;
    private final CacheService cacheService;

    public UserServiceImpl(UserRepository userRepository,
                           SmsService smsService,
                           TokenPriceService tokenPriceService,
                           TokenRepository tokenRepository,
                           OrderRepository orderRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.smsService = smsService;
        this.tokenPriceService = tokenPriceService;
        this.tokenRepository = tokenRepository;
        this.orderRepository = orderRepository;
        this.cacheService = cacheService;
    }

    private static Stream<BigDecimal> getTokenStream(Map<TokenId, BigDecimal> prices, List<OrderRepository.TokenInfo> tokenInfos) {
        return tokenInfos.stream().map(tokenInfo -> {
            TokenId id = tokenInfo.getToken().getId();
            return tokenInfo.getAmount().multiply(prices.get(new TokenId(id.getAccount(), id.getSymbol())));
        });
    }

    private static Stream<BigDecimal> getTotalStream(Map<TokenId, BigDecimal> balances, List<Token> dbTokens, Map<TokenId, BigDecimal> prices) {
        return dbTokens.stream().map(token -> {
            String contract = token.getId().getAccount();
            String symbol = token.getId().getSymbol();
            TokenId tokenId = new TokenId(contract, symbol);
            return Optional.ofNullable(balances.get(tokenId))
                    .map(balance -> balance.multiply(prices.get(tokenId)))
                    .orElse(ZERO);
        });
    }

    private static BigDecimal sum(Stream<BigDecimal> stream) {
        return stream.reduce(ZERO, BigDecimal::add, BigDecimal::add);
    }

    private static List<ErcTokenBalance> getErcTokenBalances(Map<TokenId, BigDecimal> balances, List<Token> dbTokens) {
        return dbTokens.stream().map(token -> {
            String contract = token.getId().getAccount();
            String symbol = token.getId().getSymbol();
            ErcTokenBalance eosTokenBalance = new ErcTokenBalance();
            eosTokenBalance.setCode(contract);
            eosTokenBalance.setSymbol(symbol);
            eosTokenBalance.setBalance(Optional.ofNullable(balances.get(new TokenId(contract, symbol))).orElse(ZERO));
            eosTokenBalance.setLogoUrl(token.getLogoUrl());
            return eosTokenBalance;
        }).collect(toList());
    }

    @Override
    public User findByCountryCodeAndPhone(Integer countryCode, String phone) {
        return userRepository.findByCountryCodeAndPhone(countryCode, phone)
                .filter(user -> {
                    if (user.isLocked()) {
                        throw new BusinessException(Errors.USER_LOCKED);
                    }
                    return true;
                }).orElse(null);
    }

    @Override
    public User loginOrRegister(LoginOrRegisterBody body) {
        Integer countryCode = body.getCountryCode();
        String phone = body.getPhone();
        smsService.checkSmsCode(countryCode, phone, body.getCode());
        User result = userRepository.findByCountryCodeAndPhone(countryCode, phone)
                .orElseGet(() -> userRepository.save(new User(countryCode, phone)));
        log.info("loginOrRegister:countryCode:{},phone:{}", countryCode, phone);
        return result;
    }

    @Override
    public Summary findSummary(User user, String account) {
        String key = String.format("summary:%d:%s:%s", user.getCountryCode(), user.getPhone(), account);
        return cacheService.load(key, 1, TimeUnit.MINUTES, Summary.class, () -> {
            Map<TokenId, BigDecimal> balances = new HashMap<>();
            List<Token> dbTokens = tokenRepository.findAllByEnabledTrue();
            List<ErcTokenBalance> tokens = getErcTokenBalances(balances, dbTokens);
            Map<TokenId, BigDecimal> prices = tokenPriceService.findErcPrices();
            log.info("token prices:{}", prices);
            List<OrderRepository.TokenInfo> borrowedTokenInfo = orderRepository.findBorrowedTokenInfo(user, Order.OrderStatus.uncompletedStatuses());
            List<OrderRepository.TokenInfo> loanedTokenInfo = orderRepository.findLoanedTokenInfo(user, Order.OrderStatus.uncompletedStatuses());
            BigDecimal total = sum(getTotalStream(balances, dbTokens, prices));
            BigDecimal borrowed = sum(getTokenStream(prices, borrowedTokenInfo));
            BigDecimal loaned = sum(getTokenStream(prices, loanedTokenInfo));
            Summary summary = new Summary();
            summary.setCountryCode(user.getCountryCode());
            summary.setPhone(hideWithStar(user.getPhone()));
            summary.setAccount(account);
            summary.setTotal(total);
            summary.setTokens(tokens);
            summary.setBorrowed(borrowed);
            summary.setLoaned(loaned);
            summary.setCurrency(Token.Tokens.DAI.name());
            return summary;
        });
    }
}
