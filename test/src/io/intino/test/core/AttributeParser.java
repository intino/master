package io.intino.test.core;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeParser {

    public static List<Long> toLongList(String value) {
        return Arrays.stream(value.split(",")).map(String::trim).map(Long::parseLong).collect(Collectors.toList());
    }

    public static boolean toBool(String value) {
        return Boolean.parseBoolean(value.trim().toLowerCase());
    }

    public static double toDecimal(String value) {
        return Double.parseDouble(value.trim());
    }
}
