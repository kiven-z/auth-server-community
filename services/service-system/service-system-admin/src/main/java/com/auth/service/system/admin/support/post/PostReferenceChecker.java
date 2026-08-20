package com.auth.service.system.admin.support.post;

import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 岗位存在性与计算有效校验
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PostReferenceChecker {

	private final SysPostMapper sysPostMapper;

	/**
	 * 加载岗位主表行，不存在则抛业务异常
	 * @param postId 岗位 ID
	 * @return 岗位实体
	 */
	public SysPostEntity getExistingActive(Long postId) {
		SysPostEntity existing = sysPostMapper.selectById(postId);
		if (existing == null) {
			log.warn("post not found: id={}", postId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_NOT_EXIST);
		}
		return existing;
	}

	/**
	 * 校验岗位存在且计算有效
	 * @param postId 岗位 ID
	 * @return 岗位实体
	 */
	public SysPostEntity requireEffective(Long postId) {
		SysPostEntity existing = getExistingActive(postId);
		if (sysPostMapper.countEffectiveById(postId) == 0) {
			log.warn("post unavailable: id={}", postId);
			throw new SystemBusinessException(SystemCommonResultCode.DATA_UNAVAILABLE);
		}
		return existing;
	}

}
