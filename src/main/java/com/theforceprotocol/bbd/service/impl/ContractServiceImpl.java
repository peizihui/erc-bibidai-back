package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.domain.dto.MethodName;
import com.theforceprotocol.bbd.props.ContractProperties;
import com.theforceprotocol.bbd.service.ContractService;
import com.theforceprotocol.bbd.service.TransferService;
import com.theforceprotocol.bbd.util.Web3jTypeUtil;
import org.springframework.stereotype.Service;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mingliang
 */
@Service
public class ContractServiceImpl implements ContractService {
    private TransferService transferService;
    private ContractProperties prop;

    public ContractServiceImpl(TransferService transferService, ContractProperties prop) {
        this.transferService = transferService;
        this.prop = prop;
    }

    @Override
    public String cancel(String borrower, String hash) {

        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(borrower));
        typeList.add(new Bytes32(Web3jTypeUtil.string2Bytes32(hash)));
        return transferService.sendTx(typeList, MethodName.CANCEL_ORDER.methodName, prop.getAddress());
    }

    @Override
    public String close(String borrower, String hash, String token) {
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(borrower));
        typeList.add(new Bytes32(Web3jTypeUtil.string2Bytes32(hash)));
        typeList.add(new Address(token));
        return transferService.sendTx(typeList, MethodName.FORCE_REPAY.methodName, prop.getAddress());
    }

    @Override
    public String forceRepay(String borrower, String hash, String token) {
        List<Type> typeList = new ArrayList<>();
        typeList.add(new Address(borrower));
        typeList.add(new Bytes32(Web3jTypeUtil.string2Bytes32(hash)));
        typeList.add(new Address(token));
        return transferService.sendTx(typeList, MethodName.CLOSE_POSITION.methodName, prop.getAddress());
    }
}
