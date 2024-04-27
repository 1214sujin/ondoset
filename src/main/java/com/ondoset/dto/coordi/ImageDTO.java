package com.ondoset.dto.coordi;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ImageDTO {

	private MultipartFile image;
}
