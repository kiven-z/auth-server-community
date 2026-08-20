package com.auth.module.file.api.model.dto;

import com.auth.common.core.model.response.BaseResponse;
import com.auth.module.file.api.model.enums.StoragePlatformEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;

/**
 * 文件上传结果（跨模块契约）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class FileUploadResultDTO extends BaseResponse {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 存储平台
	 */
	private StoragePlatformEnum storagePlatform;

	/**
	 * 上传模式
	 */
	private String uploadMode;

	/**
	 * 存储桶
	 */
	private String bucket;

	/**
	 * 对象键
	 */
	private String objectKey;

	/**
	 * 访问 URL
	 */
	private String url;

	/**
	 * 原始文件名
	 */
	private String originalName;

	/**
	 * 扩展名
	 */
	private String extension;

	/**
	 * MIME 类型
	 */
	private String contentType;

	/**
	 * 文件大小（字节）
	 */
	private Long size;

	/**
	 * 是否私有文件
	 */
	private Boolean isPrivate;

	/**
	 * 对象 ETag
	 */
	private String etag;

	/**
	 * 业务类型
	 */
	private String bizType;

	/**
	 * 业务主键 ID
	 */
	private String bizId;

	/**
	 * 备注
	 */
	private String remark;

}
