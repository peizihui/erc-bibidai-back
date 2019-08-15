package com.theforceprotocol.bbd.service;

import org.web3j.abi.datatypes.Type;

import java.util.List;

public interface TransferService {
    String sendTx(List<Type> typeList, String method, String contractAddress);
}
