package com.auth.service.system.admin.model.vo.me;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 个人中心用户 UI 偏好配置项
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeUserPreferenceItemVO {

	@Schema(title = "配置键")
	private String configKey;

	@Schema(title = "配置值")
	private JsonNode configValue;

}
