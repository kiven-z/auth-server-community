package com.auth.service.system.file.model.value;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于本地路径的 MultipartFile
 *
 * @author Bunny
 */
public record PathMultipartFile(String name, String originalFilename, String contentType,
		Path path) implements MultipartFile {

	@Override
	@NonNull
	public String getName() {
		return name;
	}

	@Override
	public String getOriginalFilename() {
		return originalFilename;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public boolean isEmpty() {
		return getSize() <= 0;
	}

	@Override
	public long getSize() {
		try {
			return Files.size(path);
		}
		catch (IOException exception) {
			throw new IllegalStateException("Failed to read temp export file size", exception);
		}
	}

	@Override
	@NonNull
	public byte[] getBytes() throws IOException {
		return Files.readAllBytes(path);
	}

	@Override
	@NonNull
	public InputStream getInputStream() throws IOException {
		return Files.newInputStream(path);
	}

	@Override
	public void transferTo(@NonNull File dest) throws IOException {
		Files.copy(path, dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

}
