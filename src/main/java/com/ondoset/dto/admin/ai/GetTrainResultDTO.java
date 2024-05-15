package com.ondoset.dto.admin.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetTrainResultDTO {
    private List<OnEpochDTO> results;
}
