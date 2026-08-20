package com.auth.module.file.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 按 URL 尝试删除归属用户的活跃文件
 *
 * @author Bunny
 */
@Getter
@Setter
public class OwnedFileDeleteByUrlRequest implements Serializable {

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
	 * 删除来源
	 */
	@NotBlank(message = "删除来源不能为空")
	private String deleteSource;

	/**
	 * 业务类型（可选，传入时用于进一步过滤）
	 */
	private String bizType;

}
