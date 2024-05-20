package com.ondoset.dto.ootd;

import com.ondoset.common.Enum;
import com.ondoset.domain.Enum.Weather;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class RootDTO {

	@Getter
	@Setter
	public static class req {

		@NotNull
		private String region;
		@NotNull
		private Long departTime;
		@NotNull
		private Long arrivalTime;
		@Enum(enumClass = Weather.class)
		private String weather;
		@NotNull
		private Integer lowestTemp;
		@NotNull
		private Integer highestTemp;
		@NotNull
		private MultipartFile image;
		@Size(min=1)
		private String wearingList;
	}

	@Getter
	@Setter
	public static class putReq {	// 이미지를 받지 않을 수도 있음

		@NotNull
		private String region;
		@NotNull
		private Long departTime;
		@NotNull
		private Long arrivalTime;
		@Enum(enumClass = Weather.class)
		private String weather;
		@NotNull
		private Integer lowestTemp;
		@NotNull
		private Integer highestTemp;
		private MultipartFile image;
		@Size(min=1)
		private String wearingList;
	}

	@Getter
	@Setter
	public static class res {

		private Long ootdId;
	}
}
