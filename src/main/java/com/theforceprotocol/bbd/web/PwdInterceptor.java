package com.theforceprotocol.bbd.web;

import com.theforceprotocol.bbd.props.EosContractProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class PwdInterceptor implements RequestInterceptor {
    private final EosContractProperties props;

    public PwdInterceptor(EosContractProperties props) {
        this.props = props;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("", props.getPwd());
    }
}
