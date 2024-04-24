package com.ondoset.domain.Enum;

public enum Satisfaction {

    VERY_COLD, COLD, GOOD, HOT, VERY_HOT;
    // 추웠어요 / 적당했어요 / 더웠어요

    public static Satisfaction valueOfLower(String name) {
        return Satisfaction.valueOf(name.toUpperCase());
    }
}
