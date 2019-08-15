package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.dto.TxsResp;
import org.springframework.data.domain.Pageable;

public interface UserTxService {
    TxsResp findTxs(String account, String symbol, Pageable pageable);
}
