package com.ondoset.dto.admin.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TrainModelReqDTO {
    private Integer lvc;
    private Integer cfIter;
    private Double cfLr;
    private Double cfReg;
    private Double cfW;
}
