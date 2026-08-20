package com.auth.service.system.admin.service.admin.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.auth.common.data.model.PageResponse;
import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.api.granttable.GrantTableSubjectType;
import com.auth.service.system.admin.convert.admin.user.SysUserConverter;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.mapper.authorization.GrantBindingQueryMapper;
import com.auth.service.system.admin.mapper.authorization.UserEffectiveAuthorizationQueryMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.po.user.UserSearchItemPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.model.vo.user.SysUserPageVO;
import com.auth.service.system.admin.model.vo.user.SysUserProfileVO;
import com.auth.service.system.admin.model.vo.user.SysUserSearchItemVO;
import com.auth.service.system.admin.service.admin.SysUserQueryService;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.auth.service.system.admin.support.user.UserReferenceChecker;
import com.auth.service.system.common.exception.SystemBusinessException;
import com.auth.service.system.common.exception.code.SystemCommonResultCode;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 系统用户只读查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysUserQueryServiceImpl extends ServiceImpl<SysUserMapper, UserEntity> implements SysUserQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserReferenceChecker userReferenceChecker;

	private final UserEffectiveAuthorizationQueryMapper userEffectiveAuthorizationQueryMapper;

	private final GrantBindingQueryMapper grantBindingQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysUserPageVO> getPage(SysUserPageQuery query) {
		Page<UserEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		IPage<SysUserPageRowPO> page = baseMapper.selectListByPage(pageParams, query, orderBySql);
		IPage<SysUserPageVO> voPage = page.convert(SysUserConverter.INSTANCE::toPageVo);

		auditUserDisplayService.enrichAuditUsernames(voPage, null, null);
		return PageResponse.of(voPage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserProfileVO getProfile(Long userId) {
		if (userId == null) {
			throw new SystemBusinessException(SystemCommonResultCode.PARAM_REQUIRED, "userId");
		}
		UserEntity user = userReferenceChecker.getExistingActive(userId);
		SysUserProfileVO response = SysUserConverter.INSTANCE.toProfileVo(user);
		response.setDeptCount(userEffectiveAuthorizationQueryMapper.countDeptsByUserId(userId));
		response.setPostCount(userEffectiveAuthorizationQueryMapper.countPostsByUserId(userId));
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysUserDetailVO getDetail(Long userId) {
		SysUserProfileVO profile = getProfile(userId);
		SysUserDetailVO detail = SysUserConverter.INSTANCE.fromProfile(profile);
		detail.setDirectRoleCount(
				grantBindingQueryMapper.countBoundRolesBySubject(GrantTableSubjectType.USER.name(), userId, null));
		detail.setEffectiveRoleCount(userEffectiveAuthorizationQueryMapper.countEffectiveRolesByUserId(userId, null));
		detail.setEffectivePermissionCount(
				userEffectiveAuthorizationQueryMapper.countEffectivePermissionsByUserId(userId, null));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysUserSearchItemVO> searchByKeyword(String keyword, Integer limit) {
		if (CharSequenceUtil.isBlank(keyword)) {
			return Collections.emptyList();
		}

		limit = limit == null ? 20 : Math.max(1, Math.min(limit, 50));
		List<UserSearchItemPO> searchItemList = baseMapper.searchByKeyword(keyword, limit);
		List<UserSearchItemPO> list = Objects.requireNonNullElse(searchItemList, List.of());

		return SysUserConverter.INSTANCE.toSearchItemVoList(list);
	}

}
