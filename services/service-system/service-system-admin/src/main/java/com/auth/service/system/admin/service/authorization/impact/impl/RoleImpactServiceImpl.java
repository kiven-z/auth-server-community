package com.auth.service.system.admin.service.authorization.impact.impl;

import cn.hutool.core.collection.CollUtil;
import com.auth.common.core.constants.BatchSizes;
import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.mapper.authorization.RoleImpactQueryMapper;
import com.auth.service.system.admin.model.po.reference.UserReferencePO;
import com.auth.service.system.admin.model.query.authorization.RoleImpactUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.RoleImpactRefreshResultVO;
import com.auth.service.system.admin.model.vo.reference.UserReferenceVO;
import com.auth.service.system.admin.service.authorization.impact.RoleImpactService;
import com.auth.service.system.admin.support.grant.RbacReferenceChecker;
import com.auth.service.system.authorization.dispatch.trigger.UserProfileRefreshTrigger;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 角色影响面服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class RoleImpactServiceImpl implements RoleImpactService {

	private final RbacReferenceChecker rbacReferenceChecker;

	private final RoleImpactQueryMapper roleImpactQueryMapper;

	private final UserProfileRefreshTrigger userProfileRefreshTrigger;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Transactional(readOnly = true)
	public PageResponse<UserReferenceVO> pageImpactUsers(Long roleId, RoleImpactUserPageQuery query) {
		rbacReferenceChecker.getExisting(roleId);
		long total = roleImpactQueryMapper.countImpactUsersByRoleId(roleId, query);

		Page<UserReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<UserReferencePO> page = roleImpactQueryMapper.selectImpactUsersByRoleIdPage(pageParams, roleId, query);

		IPage<UserReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toUserReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RoleImpactRefreshResultVO refreshImpactedProfiles(Long roleId, List<Long> userIds) {
		rbacReferenceChecker.getExisting(roleId);

		List<Long> targets = resolveRefreshTargets(roleId, userIds);
		if (CollUtil.isEmpty(targets)) {
			RoleImpactRefreshResultVO empty = new RoleImpactRefreshResultVO();
			empty.setRefreshedUserCount(0L);
			return empty;
		}

		for (List<Long> chunk : CollUtil.split(targets, BatchSizes.SIZE_500)) {
			userProfileRefreshTrigger.refreshByUserIds(chunk);
		}

		RoleImpactRefreshResultVO resultVO = new RoleImpactRefreshResultVO();
		resultVO.setRefreshedUserCount((long) targets.size());
		return resultVO;
	}

	/**
	 * 解析待刷新用户：空入参刷全部影响用户；有入参取与影响面交集
	 * @param roleId 角色 ID
	 * @param userIds 指定用户
	 * @return 去重后的用户 ID
	 */
	private List<Long> resolveRefreshTargets(Long roleId, List<Long> userIds) {
		List<Long> impactIds = CollUtil.emptyIfNull(roleImpactQueryMapper.selectImpactUserIdsByRoleId(roleId));
		if (CollUtil.isEmpty(userIds)) {
			return impactIds.stream().filter(Objects::nonNull).distinct().toList();
		}

		Set<Long> impactSet = new HashSet<>(impactIds);
		return userIds.stream().filter(Objects::nonNull).filter(impactSet::contains).distinct().toList();
	}

}
