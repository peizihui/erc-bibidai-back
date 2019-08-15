package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.service.TransferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.web3j.tx.Transfer.GAS_LIMIT;
import static org.web3j.tx.gas.DefaultGasProvider.GAS_PRICE;

/**
 * @author Mingliang
 */
@Slf4j
@Service
public class TransferServiceImpl implements TransferService {
    @Resource
    private Web3jService web3jService;
    private final static String SENDER_PRIVATE_KEY = "a0cfbe72ffcdb97bea1be3df169db132909ce70fe1283d059df29c4dff230c4e";

    @Override
    public String sendTx(List<Type> typeList, String method, String contractAddress) {
        Web3j web3j = web3jService.getClient();
        Credentials credentials = Credentials.create(SENDER_PRIVATE_KEY);
        String data = encodeTransferData(method, typeList);
        RawTransaction rawTransaction = RawTransaction.createTransaction(getNonce(),
                GAS_PRICE, GAS_LIMIT, contractAddress, data);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);
        System.out.println("hexValue:" + hexValue);
        EthSendTransaction ethSendTransaction = null;
        try {
            ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (ethSendTransaction == null) {
            System.out.println("no transaction hash got！");
        }
        if (ethSendTransaction != null) {
            String transactionHash = ethSendTransaction.getTransactionHash();
            System.out.println("the transaction hash is：" + transactionHash);
            System.out.println("the transaction RawResponse is：" + ethSendTransaction.getRawResponse());
            System.out.println("the transaction Result is：" + ethSendTransaction.getResult());
            System.out.println("the transaction JsonRpc is：" + ethSendTransaction.getJsonrpc());
            System.out.println("the transaction ID is：" + ethSendTransaction.getId());
            if (ethSendTransaction.getError() != null) {
                if (ethSendTransaction.getError().getCode() != 1) {
                    System.out.println("transaction failed！");
                    System.out.println("error data:" + ethSendTransaction.getError().getData());
                    System.out.println("error msg:" + ethSendTransaction.getError().getMessage());
                    System.out.println("error code:" + ethSendTransaction.getError().getCode());
                } else {
                    return transactionHash;
                }
            }
        }
        return null;
    }

    private BigInteger getNonce() {
        Web3j web3j = web3jService.getClient();
        Credentials credentials = Credentials.create(SENDER_PRIVATE_KEY);
        EthGetTransactionCount ethGetTransactionCount;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            return ethGetTransactionCount.getTransactionCount();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String encodeTransferData(String method, List<Type> typeList) {
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Function function = new Function(method, typeList, outputParameters);
        return FunctionEncoder.encode(function);
    }
}
