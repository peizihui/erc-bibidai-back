package com.theforceprotocol.bbd.util;

import org.apache.commons.lang.StringUtils;

import java.util.Optional;

public class DataUtils {
    public static String hideWithStar(String input) {
        return Optional.ofNullable(input)
                .map(str -> {
                    int starSize = (int) Math.ceil(str.length() * 1.0 / 3);
                    int totalSize = str.length();
                    int leftSize = (totalSize - starSize) / 2;
                    int rightSize = totalSize - leftSize - starSize;
                    String left = str.substring(0, leftSize);
                    String middle = StringUtils.repeat("*", starSize);
                    String right = str.substring(totalSize - rightSize);
                    return String.format("%s%s%s", left, middle, right);
                }).orElse(null);
    }
}
