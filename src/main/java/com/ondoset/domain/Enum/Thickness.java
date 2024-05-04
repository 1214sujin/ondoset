package com.ondoset.domain.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Thickness {

    THIN("얇은"),
    NORMAL("적당한"),
    THICK("두꺼운");

    private final String name;

    public static Thickness valueOfLower(String name) {
        if (name == null) return null;
        return Thickness.valueOf(name.toUpperCase());
    }
}
