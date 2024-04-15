package com.ondoset.domain.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    TOP("상의"),
    BOTTOM("하의"),
    OUTER("아우터"),
    SHOE("신발"),
    ACC("액세서리");

    private final String name;
}
