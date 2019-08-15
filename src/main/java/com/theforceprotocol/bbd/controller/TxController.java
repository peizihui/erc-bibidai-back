package com.theforceprotocol.bbd.controller;


import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.service.UserTxService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/txs")
public class TxController {
    private final UserTxService userTxService;

    public TxController(UserTxService userTxService) {
        this.userTxService = userTxService;
    }

    @GetMapping("/{account}/{symbol}")
    public JSONObject findTxs(@PathVariable String account, @PathVariable String symbol, Pageable pageable) {
        return JsonBuilder.successBuilder(userTxService.findTxs(account, symbol, pageable));
    }
}
