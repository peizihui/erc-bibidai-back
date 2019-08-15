package com.theforceprotocol.bbd.service.impl;

import com.theforceprotocol.bbd.service.OrderService;
import com.theforceprotocol.bbd.service.RedisLockService;
import com.theforceprotocol.bbd.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    private final OrderService orderService;
    private final RedisLockService redisLockService;

    public TaskServiceImpl(OrderService orderService, RedisLockService redisLockService) {
        this.orderService = orderService;
        this.redisLockService = redisLockService;
    }

    @Override
    public void handlePendingOrders() throws Exception {
        redisLockService.process("handle:pending:orders", () -> {
            orderService.findPendingOrders().forEach(order -> {
                try {
                    orderService.handlePendingOrder(order);
                } catch (Exception e) {
                    log.error("handle pending order failed:{}", order.getOrderNumber(), e);
                }
            });
        });
    }

    @Override
    public void handleCancelableOrders() throws Exception {
        redisLockService.process("handle:cancelable:orders", () -> {
            orderService.findCancelableOrders().forEach(order -> {
                try {
                    orderService.cancel(order, "timeout");
                } catch (Exception e) {
                    log.error("handle cancelable order failed:{}", order.getOrderNumber(), e);
                }
            });
        });
    }

    @Override
    public void handleForceRepayableOrders() throws Exception {
        redisLockService.process("handle:force:repayable:orders", () -> {
            orderService.findForceRepayableOrders().forEach(order -> {
                try {
                    orderService.forceRepay(order, "deadline");
                } catch (Exception e) {
                    log.error("handle force repayable order failed:{}", order.getOrderNumber(), e);
                }
            });
        });
    }

    @Override
    public void handleValueChangingOrders() throws Exception {
        redisLockService.process("handle:value:changing:orders", () -> {
            orderService.findNotCompletedOrders().forEach(order -> {
                try {
                    orderService.handleValueChangingOrder(order);
                } catch (Exception e) {
                    log.error("handle value changing order failed:{}", order.getOrderNumber(), e);
                }
            });
        });
    }

    @Override
    public void handleWarningOrders() throws Exception {
        redisLockService.process("handle:warning:orders", () -> {
            orderService.findWarningOrders().forEach(order -> {
                try {
                    orderService.handleWarningOrder(order, "warning");
                } catch (Exception e) {
                    log.error("handle warning order failed:{}", order.getOrderNumber(), e);
                }
            });
        });
    }
}
