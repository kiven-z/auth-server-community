package com.auth.service.system.authorization.model.po;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 授权失效失败率趋势分桶统计 PO
 *
 * @author Bunny
 */
@Getter
@Setter
public class AuthorizationInvalidationFailureRateTrendBucketPO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(title = "时间桶（日期字符串）")
	private String bucket;

	@Schema(title = "总记录数")
	private Long totalCount;

	@Schema(title = "待重试记录数")
	private Long failedCount;

	@Schema(title = "死信记录数")
	private Long deadCount;

}
