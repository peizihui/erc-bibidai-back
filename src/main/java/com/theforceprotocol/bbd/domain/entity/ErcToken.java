package com.theforceprotocol.bbd.domain.entity;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * @author Mingliang
 */
@Data
@Entity
@NoArgsConstructor
public class ErcToken extends BaseEntity {
    private static final long serialVersionUID = -2241628118214873614L;
    @Id
    @GeneratedValue
    private Integer id;
    private String address;
    private String symbol;
    @Enumerated
    private Token.TokenType type;
    private Integer decimals;
    private String logoUrl;
}
