package com.auth.service.auth.model.query;

import com.auth.common.core.model.query.PageQueryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 在线用户分页查询条件
 *
 * @author Bunny
 */
@Schema(name = "OnlineUserPageQuery", title = "在线用户查询")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class OnlineUserPageQuery extends PageQueryRequest {

	@Schema(title = "用户 ID")
	private Long userId;

}
