package com.auth.service.system.file.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文件隐私切换表单
 *
 * @author Bunny
 */
@Schema(name = "FilePrivacyUpdateForm", title = "文件隐私切换表单")
@Getter
@Setter
public class FilePrivacyUpdateForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "文件记录主键集合", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotEmpty(message = "文件记录主键集合不能为空")
	private List<Long> ids;

	@Schema(title = "目标是否私有", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "目标是否私有不能为空")
	private Boolean isPrivate;

}
