package com.auth.module.file.api.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 文件上传元数据
 *
 * @author Bunny
 */
@Getter
@Setter
public class FileUploadMetadata implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 存储平台
	 */
	@Size(max = 32, message = "存储平台长度不能超过32个字符")
	private String storagePlatform;

	/**
	 * 业务类型，如 avatar、attachment
	 */
	@Size(max = 64, message = "业务类型长度不能超过64个字符")
	@NotBlank(message = "业务类型不能为空")
	private String bizType;

	/**
	 * 业务主键 ID
	 */
	@Size(max = 64, message = "业务主键 ID 长度不能超过64个字符")
	private String bizId;

	/**
	 * 备注
	 */
	@Size(max = 500, message = "备注长度不能超过500个字符")
	private String remark;

	/**
	 * 是否私有文件
	 */
	private Boolean isPrivate;

}
