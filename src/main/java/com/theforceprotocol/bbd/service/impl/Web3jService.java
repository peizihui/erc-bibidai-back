package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.props.Web3jProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/**
 * @author Mingliang
 */
@Slf4j
@Repository
public class Web3jService {
    private Web3jProperties props;
    private volatile static Web3j WEB3J;

    public Web3jService(Web3jProperties props) {
        this.props = props;
    }

    public Web3j getClient() {
        if (WEB3J == null) {
            synchronized (Web3jService.class) {
                if (WEB3J == null) {
                    WEB3J = Web3j.build(new HttpService(props.getHost()));
                }
            }
        }
        return WEB3J;
    }
}
