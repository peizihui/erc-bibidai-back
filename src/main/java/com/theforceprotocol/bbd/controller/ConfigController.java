package com.theforceprotocol.bbd.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.theforceprotocol.bbd.props.PledgeProperties;
import com.theforceprotocol.bbd.service.PledgeConfigService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {
    private final PledgeConfigService pledgeConfigService;
    private final PledgeProperties props;

    public ConfigController(PledgeConfigService pledgeConfigService, PledgeProperties props) {
        this.pledgeConfigService = pledgeConfigService;
        this.props = props;
    }

    @GetMapping
    public JSONObject findConfig() {
        return JsonBuilder.successBuilder(pledgeConfigService.findConfigs());
    }

    @GetMapping("/extra")
    public JSONObject findExtraInfo() {
        return JsonBuilder.successBuilder(ImmutableMap.of("banner", props.getBanner()));
    }
}
