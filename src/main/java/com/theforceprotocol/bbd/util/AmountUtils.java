package com.theforceprotocol.bbd.util;

import java.math.BigDecimal;
import java.util.Objects;

public class AmountUtils {
    public static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    public static BigDecimal bd(int value) {
        return new BigDecimal(value);
    }

    public static boolean veq(BigDecimal v1, BigDecimal v2) {
        return Objects.requireNonNull(v1).compareTo(Objects.requireNonNull(v2)) == 0;
    }

    public static boolean gt(BigDecimal v1, BigDecimal v2) {
        return Objects.requireNonNull(v1).compareTo(Objects.requireNonNull(v2)) > 0;
    }

    public static boolean gte(BigDecimal v1, BigDecimal v2) {
        return Objects.requireNonNull(v1).compareTo(Objects.requireNonNull(v2)) >= 0;
    }

    public static boolean lt(BigDecimal v1, BigDecimal v2) {
        return Objects.requireNonNull(v1).compareTo(Objects.requireNonNull(v2)) < 0;
    }

    public static boolean lte(BigDecimal v1, BigDecimal v2) {
        return Objects.requireNonNull(v1).compareTo(Objects.requireNonNull(v2)) <= 0;
    }

}
