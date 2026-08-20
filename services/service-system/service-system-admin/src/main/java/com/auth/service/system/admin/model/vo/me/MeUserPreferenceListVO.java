package com.auth.service.system.admin.model.vo.me;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 个人中心用户 UI 偏好配置列表
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
public class MeUserPreferenceListVO {

	@Schema(title = "偏好配置项列表")
	private List<MeUserPreferenceItemVO> items;

}
