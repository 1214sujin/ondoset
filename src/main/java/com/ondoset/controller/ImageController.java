package com.ondoset.controller;

import com.ondoset.controller.advice.CustomException;
import com.ondoset.controller.advice.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Log4j2
@RequiredArgsConstructor
@RestController
public class ImageController {

	@Value("${com.ondoset.resources.path}")
	private String resourcesPath;

	@GetMapping("/images/{folderName}/{fileName}")
	public ResponseEntity<Resource> getImages(@PathVariable("folderName") String folderName, @PathVariable("fileName") String fileName) {

		Resource resource = new FileSystemResource(resourcesPath+File.separator+folderName+File.separator+fileName);

		HttpHeaders headers = new HttpHeaders();

		try {
			headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
		} catch (IOException e) {
			throw new CustomException(ResponseCode.COM5000);
		}
		return ResponseEntity.ok().headers(headers).body(resource);
	}
}
