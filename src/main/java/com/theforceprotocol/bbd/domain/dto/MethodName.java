package com.theforceprotocol.bbd.domain.dto;

public enum MethodName {
    /**/
    BORROW("borrow", "0x8984523b"),
    CANCEL_ORDER("cancelOrder", "0x84b2167c"),
    REPAY("repay", "0xbcaf0f86"),
    LEND("lend", "0xe5465026"),
    CALL_MARGIN("callmargin", ""),
    FORCE_REPAY("forcerepay", ""),
    CLOSE_POSITION("closepstion", "");
    public String methodName;
    public String methodCode;

    MethodName(String methodName, String methodCode) {
        this.methodName = methodName;
        this.methodCode = methodCode;
    }
}
