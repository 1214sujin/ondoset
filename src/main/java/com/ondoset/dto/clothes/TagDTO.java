package com.ondoset.dto.clothes;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TagDTO {

	private List<Tag> top;
	private List<Tag> bottom;
	private List<Tag> outer;
	private List<Tag> shoe;
	private List<Tag> acc;
}
