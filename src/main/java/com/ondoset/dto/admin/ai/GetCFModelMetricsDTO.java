package com.ondoset.dto.admin.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class GetCFModelMetricsDTO {
    private List<CFModelMetricsDTO> result;
}
