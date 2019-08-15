package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.domain.dto.TxsResp;
import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.repository.UserTxRepository;
import com.theforceprotocol.bbd.service.UserTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.theforceprotocol.bbd.util.PageUtils.sorted;
import static com.theforceprotocol.bbd.web.ContextHolder.requiredCurrentUser;

@Slf4j
@Service
public class UserTxServiceImpl implements UserTxService {
    private final UserTxRepository userTxRepository;

    public UserTxServiceImpl(UserTxRepository userTxRepository) {
        this.userTxRepository = userTxRepository;
    }

    @Override
    public TxsResp findTxs(String account, String symbol, Pageable pageable) {
        User user = requiredCurrentUser();
        Page<TxsResp.Tx> page = userTxRepository.findTxs(user, account, symbol, sorted(pageable))
                .map(userTx -> {
                    TxsResp.Tx tx = new TxsResp.Tx();
                    tx.setAmount(userTx.getAmount());
                    tx.setOrderNumber(userTx.getOrder().getOrderNumber());
                    tx.setTs(userTx.getCreatedDate());
                    return tx;
                });
        log.info("find txs by user:{},account:{},symbol:{},pageable:{},result:{}",
                user, account, symbol, pageable, page);
        BigDecimal sum = userTxRepository.findSumAmount(user, account, symbol).orElse(BigDecimal.ZERO);
        log.info("find sum amount by user:{},account:{},symbol:{},result:{}", user, account, symbol, sum);
        return new TxsResp(sum, page);
    }
}
