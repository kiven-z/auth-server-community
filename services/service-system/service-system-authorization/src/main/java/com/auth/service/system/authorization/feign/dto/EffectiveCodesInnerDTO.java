package com.auth.service.system.authorization.feign.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 用户生效角色码与权限码（内部 Feign 传输）
 *
 * @author Bunny
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EffectiveCodesInnerDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "角色码")
	private List<String> roleCodes;

	@Schema(title = "权限码")
	private List<String> permissionCodes;

}
