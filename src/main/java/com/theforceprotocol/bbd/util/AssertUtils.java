package com.theforceprotocol.bbd.util;

import com.theforceprotocol.bbd.exception.BusinessException;

public class AssertUtils {
    private AssertUtils() {
    }

    public static boolean isTrue(Boolean result, Errors errors) {
        if (Boolean.TRUE.equals(result)) {
            return true;
        }
        throw new BusinessException(errors);
    }
}
