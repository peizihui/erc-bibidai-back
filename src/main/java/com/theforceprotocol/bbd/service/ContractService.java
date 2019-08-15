package com.theforceprotocol.bbd.service;

/**
 * @author Mingliang
 */
public interface ContractService {
    String cancel(String borrower, String hash);

    String close(String borrower, String hash, String token);

    String forceRepay(String borrower, String hash, String token);
}