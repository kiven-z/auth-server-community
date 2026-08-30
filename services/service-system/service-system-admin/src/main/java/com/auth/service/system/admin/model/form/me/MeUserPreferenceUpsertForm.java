package com.auth.service.system.admin.model.form.me;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 当前用户 UI 偏好配置 upsert 表单
 *
 * @author Bunny
 */
@Schema(name = "MeUserPreferenceUpsertForm", title = "当前用户偏好配置更新")
@Getter
@Setter
public class MeUserPreferenceUpsertForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "配置键", requiredMode = Schema.RequiredMode.REQUIRED, example = "ui.layout")
	@NotBlank(message = "配置键不能为空")
	private String configKey;

	@Schema(title = "配置值 JSON 对象", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "配置值不能为空")
	private transient JsonNode configValue;

}
