package com.ondoset.dto.member;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class ProfilePicDTO {

	private MultipartFile image;
}
