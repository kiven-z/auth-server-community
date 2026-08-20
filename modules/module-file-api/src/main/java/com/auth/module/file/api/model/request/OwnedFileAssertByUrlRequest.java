package com.auth.module.file.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 校验 URL 对应文件是否归属指定用户
 *
 * @author Bunny
 */
@Getter
@Setter
public class OwnedFileAssertByUrlRequest implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 文件访问 URL
	 */
	@NotBlank(message = "文件访问 URL 不能为空")
	private String url;

	/**
	 * 归属用户 ID
	 */
	@NotNull(message = "归属用户 ID 不能为空")
	private Long ownerUserId;

	/**
	 * 业务类型
	 */
	@NotBlank(message = "业务类型不能为空")
	private String bizType;

}
