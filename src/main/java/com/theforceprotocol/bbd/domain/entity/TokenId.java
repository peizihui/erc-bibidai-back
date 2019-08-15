package com.theforceprotocol.bbd.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class TokenId implements Serializable {
    private static final long serialVersionUID = -4093119980703701403L;
    @Column(length = 12, nullable = false)
    private String account;
    @Column(length = 10)
    private String symbol;
}
