package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.dto.*;
import com.theforceprotocol.bbd.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    Order createOrder(OrderCreateRequestBody body);

    OrderCalcRespBody calc(OrderCalcRequestBody body);

    Page<Order> findOrders(Pageable pageable);

    Optional<Order> findOrder(String orderNumber);

    void borrow(OrderBody body) throws Exception;

    void loan(ActionBody body) throws Exception;

    void buyIn(ActionBody body) throws Exception;

    void repay(ActionBody body) throws Exception;

    void forceRepay(Order order, String memo) throws Exception;

    void close(Order order, String memo) throws Exception;

    void cancel(Order order, String memo) throws Exception;

    void cancel(OrderBody body) throws Exception;

    List<Order> findPendingOrders();

    Page<Order> findMyOrders(Status status, Pageable pageable);

    List<Order> findCancelableOrders();

    List<Order> findForceRepayableOrders();

    List<Order> findNotCompletedOrders();

    void handleValueChangingOrder(Order order) throws Exception;

    void alarm(Order order, BigDecimal pledgeRate, String memo);

    void handlePendingOrder(Order order) throws Exception;

    BuyInCalcRespBody calc(BuyInCalcRequestBody body);

    List<Order> findWarningOrders();

    void handleWarningOrder(Order order, String memo) throws Exception;
}
