package com.theforceprotocol.bbd.controller;

import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.domain.dto.*;
import com.theforceprotocol.bbd.domain.entity.Order;
import com.theforceprotocol.bbd.service.OrderService;
import com.theforceprotocol.bbd.util.AssertUtils;
import com.theforceprotocol.bbd.util.Errors;
import com.theforceprotocol.bbd.util.JsonBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/calc")
    public JSONObject calc(@Valid @RequestBody OrderCalcRequestBody body) {
        return JsonBuilder.successBuilder(orderService.calc(body));
    }

    @PostMapping("/cancel")
    public JSONObject cancel(@Valid @RequestBody OrderBody body) throws Exception {
        orderService.cancel(body);
        return JsonBuilder.successBuilder(null);
    }

    @PostMapping("/borrow")
    public JSONObject borrow(@Valid @RequestBody OrderBody body) throws Exception {
        orderService.borrow(body);
        return JsonBuilder.successBuilder(null);
    }

    @PostMapping("/loan")
    public JSONObject loan(@Valid @RequestBody ActionBody body) throws Exception {
        orderService.loan(body);
        return JsonBuilder.successBuilder(null);
    }

    @PostMapping("/buy_in")
    public JSONObject buyIn(@Valid @RequestBody ActionBody body) throws Exception {
        orderService.buyIn(body);
        return JsonBuilder.successBuilder(null);
    }

    @PostMapping("/buy_in/calc")
    public JSONObject calc(@Valid @RequestBody BuyInCalcRequestBody body) {
        return JsonBuilder.successBuilder(orderService.calc(body));
    }

    @PostMapping("/repay")
    public JSONObject repay(@Valid @RequestBody ActionBody body) throws Exception {
        orderService.repay(body);
        return JsonBuilder.successBuilder(null);
    }

    @PostMapping("/create")
    public JSONObject createOrder(@Valid @RequestBody OrderCreateRequestBody body) {
        String borrowedAccount = body.getBorrowedAccount();
        String borrowedSymbol = body.getBorrowedSymbol();
        String pledgeAccount = body.getPledgeAccount();
        String pledgeSymbol = body.getPledgeSymbol();
        AssertUtils.isTrue(!Objects.equals(borrowedAccount, pledgeAccount) ||
                !Objects.equals(borrowedSymbol, pledgeSymbol), Errors.SAME_TOKEN);
        Order order = orderService.createOrder(body);
        OrderCreateRespBody respBody = new OrderCreateRespBody(order.getOrderNumber(), order.getPledgeRate(), order.getReallyBorrowedAmount());
        return JsonBuilder.successBuilder(respBody);
    }

    @GetMapping("/list")
    public JSONObject findOrders(Pageable pageable) {
        return JsonBuilder.successBuilder(orderService.findOrders(pageable).map(Order::convert));
    }

    @GetMapping("/mine")
    public JSONObject findMyOrders(@RequestParam(required = false, defaultValue = "ALL") Status status,
                                   Pageable pageable) {
        return JsonBuilder.successBuilder(orderService.findMyOrders(status, pageable).map(Order::convert));
    }

    @GetMapping
    public JSONObject findOrder(String orderNumber) {
        OrderDetailRespBody respBody = orderService.findOrder(orderNumber).map(Order::convert).orElse(null);
        return JsonBuilder.successBuilder(respBody);
    }
}
