package com.auth.module.file.api.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件上传模式枚举
 *
 * @author Bunny
 */
@Getter
@AllArgsConstructor
public enum FileUploadMode {

	/**
	 * 简单直传
	 */
	SIMPLE("SIMPLE"),;

	/**
	 * 模式编码
	 */
	private final String code;

}
