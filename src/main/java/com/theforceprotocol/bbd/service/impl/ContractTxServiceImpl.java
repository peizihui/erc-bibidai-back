package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.service.ContractTxService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;

import java.io.IOException;

/**
 * @author Mingliang
 */
@Slf4j
@Service
public class ContractTxServiceImpl implements ContractTxService {

    private final Web3jService web3jService;

    public ContractTxServiceImpl(Web3jService web3jService) {
        this.web3jService = web3jService;
    }

    @Override
    public String findTxData(String txId) throws IOException {
        log.info("find tx action by txId:{}", txId);
        Web3j web3j = web3jService.getClient();
        EthTransaction transaction = web3j.ethGetTransactionByHash(txId).send();
        String txInputData = transaction.getResult().getInput();
        log.info("get txInputData:{} according to txId", txInputData);
        return txInputData;
    }
}
