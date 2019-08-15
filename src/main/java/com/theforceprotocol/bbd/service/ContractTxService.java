package com.theforceprotocol.bbd.service;

import java.io.IOException;

/**
 * @author Mingliang
 */
public interface ContractTxService {
    String findTxData(String txId) throws IOException;
}
