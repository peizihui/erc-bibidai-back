package com.theforceprotocol.bbd.service;

public interface TaskService {
    void handlePendingOrders() throws Exception;

    void handleCancelableOrders() throws Exception;

    void handleForceRepayableOrders() throws Exception;

    void handleValueChangingOrders() throws Exception;

    void handleWarningOrders() throws Exception;
}
