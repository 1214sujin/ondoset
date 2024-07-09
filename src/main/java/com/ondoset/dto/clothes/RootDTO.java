package com.ondoset.dto.clothes;

import com.ondoset.config.Enum;
import com.ondoset.domain.Enum.Thickness;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

public class RootDTO {

	@Getter
	@Setter
	public static class req {

		@NotEmpty
		private String name;
		@NotNull
		private Long tagId;
		@Enum(enumClass = Thickness.class, nullable = true)
		private String thickness;
		private MultipartFile image;
	}

	@Getter
	@Setter
	public static class putReq {

		@NotEmpty
		private String name;
		@NotNull
		private Long tagId;
		@Enum(enumClass = Thickness.class, nullable = true)
		private String thickness;
		@NotNull
		private MultipartFile image;
	}

	@Getter
	@Setter
	public static class patchReq {

		@NotEmpty
		private String name;
		@NotNull
		private Long tagId;
		@Enum(enumClass = Thickness.class, nullable = true)
		private String thickness;
	}

	@Getter
	@Setter
	public static class res {

		private Long clothesId;
	}
}
