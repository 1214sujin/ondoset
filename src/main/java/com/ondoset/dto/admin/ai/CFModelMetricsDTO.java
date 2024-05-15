package com.ondoset.dto.admin.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CFModelMetricsDTO {
    private Long modelId;
    private Double version;
    private LocalDateTime date;
    private double loss;
    private double precision;
    private double recall;
    private double f1;
}
