package com.auth.service.system.admin.model.query.me;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 个人中心登录日志分页查询
 *
 * @author Bunny
 */
@Schema(name = "MeLoginLogPageQuery", title = "个人中心登录日志查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class MeLoginLogPageQuery extends PageQueryRequest {

	@Schema(title = "登录结果")
	private Integer loginResult;

	@Schema(title = "登录方式")
	private String loginType;

}
