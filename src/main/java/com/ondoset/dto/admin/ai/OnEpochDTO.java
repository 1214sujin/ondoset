package com.ondoset.dto.admin.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class OnEpochDTO {
    private String type;
    private int epoch;
    private double value;
}
