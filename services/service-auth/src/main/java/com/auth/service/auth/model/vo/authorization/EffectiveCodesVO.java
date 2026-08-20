package com.auth.service.auth.model.vo.authorization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 内部接口返回：用户生效角色码与权限码
 *
 * @author Bunny
 */
@Schema(title = "生效角色与权限码")
@Getter
@Setter
@ToString
public class EffectiveCodesVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色编码列表")
	private List<String> roleCodes;

	@Schema(title = "权限编码列表")
	private List<String> permissionCodes;

}
