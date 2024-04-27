package com.ondoset.domain.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Weather {

    SUNNY("맑음"),
    PARTLY_CLOUDY("조금 흐림"),
    CLOUDY("흐림"),
    RAINY("비"),
    SNOWY("눈"),
    SLEET("눈비");

    private final String name;

    public static Weather valueOfLower(String name) {
        return Weather.valueOf(name.toUpperCase());
    }
}
