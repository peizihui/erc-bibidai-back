package com.theforceprotocol.bbd.domain.entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static com.theforceprotocol.bbd.domain.entity.Token.TokenType.*;

@Data
@Entity
@NoArgsConstructor
public class Token extends BaseEntity {
    private static final long serialVersionUID = 7339456126223056937L;
    @EmbeddedId
    private TokenId id;
    private Integer decimals;
    @Enumerated
    private TokenType type;
    private String logoUrl;
    private boolean enabled = true;
    @OneToMany(mappedBy = "pledgeToken", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Order> pledgeOrders = new ArrayList<>();
    @OneToMany(mappedBy = "borrowedToken", cascade = {CascadeType.ALL}, orphanRemoval = true)
    private List<Order> borrowedOrders = new ArrayList<>();

    public enum TokenType {
        /**
         * Token类别
         **/
        PLEDGE,
        BORROWED,
        ALL
    }

    @Getter
    public enum Tokens {
        /**
         * 将在启动时生成到数据库
         **/
        BNB("0xb48c5c873d93024a934a626ade334d71813527f6", "binance-coin", 8, ALL),
        DAI("0x25355c00fd0bcD6Ca0E3016d8ADeC8Fbfd56a193", "dai", 8, BORROWED),
        FOR("0x87ab9c99c8bdb5a9264c7beb71fad534f950a144", "forceprotocol", 8, ALL),
        HT("0xc82277e21c1569cc153e093d6462cb2262568ab9", "huobi-token", 8, PLEDGE),
        LRC("0x5463a11816d3a59e2687cec1edb1c0e92d928de8", "loopring", 8, ALL),
        ZRX("0xd86e720646b82bcc014801af5fd8722d89db712e", "0x", 8, BORROWED);
        private final String account;
        private final String pair;
        private final Integer decimals;
        private final TokenType type;
        private final String logoUrl = "";

        Tokens(String account, String pair, Integer decimals, TokenType type) {
            this.account = account;
            this.pair = pair;
            this.decimals = decimals;
            this.type = type;
        }

        public Token toToken() {
            Token result = new Token();
            result.setId(new TokenId(account, name()));
            result.setDecimals(decimals);
            result.setType(type);
            result.setLogoUrl(logoUrl);
            return result;
        }
    }
}
