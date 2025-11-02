package com.unitalk.carbets;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
public enum CarBrand {
    HUMMER("Hummer"),
    FERRARI("Ferrari"),
    BMW("BMW"),
    AUDI("Audi"),
    HONDA("Honda");

    private final String name;

    public static boolean contains(String name) {
        return Arrays.stream(CarBrand.values())
                .anyMatch(value -> value.name.equalsIgnoreCase(name));
    }

    public static String get(String name) {
        return Objects.requireNonNull(Arrays.stream(values())
                .filter(value -> value.name.equalsIgnoreCase(name))
                .findFirst().orElse(null)).name;
    }
}
