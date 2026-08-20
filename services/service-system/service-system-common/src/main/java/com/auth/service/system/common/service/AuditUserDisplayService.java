package com.auth.service.system.common.service;

import com.auth.common.core.model.response.BaseResponse;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.ToLongFunction;

/**
 * 审计用户展示名服务
 *
 * @author Bunny
 */
public interface AuditUserDisplayService {

	/**
	 * 批量解析用户名
	 * @param userIds 为 null 或空时返回空 Map
	 * @return 仅包含能解析到的 id → username
	 */
	Map<Long, String> mapUsernamesByIds(Collection<Long> userIds);

	/**
	 * 为分页结果填充审计用户名，并可选填充额外用户字段（一次批量查询）
	 * @param page MyBatis-Plus 分页结果
	 * @param extraUserIdGetter 额外用户 ID 读取；为 null 时仅填充审计列
	 * @param extraUsernameSetter 额外用户名写入；为 null 时仅填充审计列
	 * @param <T> 继承 {@link BaseResponse} 的行类型
	 */
	<T extends BaseResponse> void enrichAuditUsernames(IPage<T> page, ToLongFunction<T> extraUserIdGetter,
			BiConsumer<T, String> extraUsernameSetter);

	/**
	 * 为列表填充审计用户名，并可选填充额外用户字段（一次批量查询）
	 * @param records 行列表
	 * @param extraUserIdGetter 额外用户 ID 读取；为 null 时仅填充审计列
	 * @param extraUsernameSetter 额外用户名写入；为 null 时仅填充审计列
	 * @param <T> 继承 {@link BaseResponse} 的行类型
	 */
	<T extends BaseResponse> void enrichAuditUsernames(List<T> records, ToLongFunction<T> extraUserIdGetter,
			BiConsumer<T, String> extraUsernameSetter);

}
