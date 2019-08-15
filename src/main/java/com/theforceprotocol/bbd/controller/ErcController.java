package com.theforceprotocol.bbd.controller;

import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.service.TokenPriceService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Mingliang
 */
@RestController
@RequestMapping("/erc")
public class ErcController {
    @Resource
    private TokenPriceService tokenPriceService;

    @GetMapping("/price")
    public JSONObject getTokenPrice() {
        return JsonBuilder.successBuilder(tokenPriceService.findErcPrices());
    }
}
