package com.theforceprotocol.bbd.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@AllArgsConstructor
public class CachedSmsCode implements Serializable {
    private static final long serialVersionUID = -5320982796377768596L;
    private String code;
    private Instant time;
}
