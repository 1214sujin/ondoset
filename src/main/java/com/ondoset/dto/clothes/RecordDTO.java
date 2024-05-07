package com.ondoset.dto.clothes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecordDTO {

	private Long date;
	private List<ClothesDTO> clothesList;
}
