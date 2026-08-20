package com.auth.common.web.multipart;

import lombok.Builder;
import lombok.Value;
import org.springframework.lang.NonNull;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 基于内存字节，用于程序化上传
 *
 * @author Bunny
 */
@Value
@Builder
public class InMemoryMultipartFile implements MultipartFile {

	/**
	 * 表单字段名
	 */
	String name;

	/**
	 * 原始文件名
	 */
	String originalFilename;

	/**
	 * MIME 类型
	 */
	String contentType;

	/**
	 * 文件字节
	 */
	@Builder.Default
	byte[] content = new byte[0];

	@Override
	public boolean isEmpty() {
		return safeContent().length == 0;
	}

	@Override
	public long getSize() {
		return safeContent().length;
	}

	@Override
	@NonNull
	public byte[] getBytes() {
		return safeContent();
	}

	@Override
	@NonNull
	public InputStream getInputStream() {
		return new ByteArrayInputStream(safeContent());
	}

	@Override
	public void transferTo(@NonNull File dest) throws IOException {
		FileCopyUtils.copy(safeContent(), dest);
	}

	@NonNull
	private byte[] safeContent() {
		return Objects.requireNonNullElse(content, new byte[0]);
	}

}
