package com.auth.service.system.admin.support.user;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.service.system.admin.convert.admin.user.SysUserConverter;
import com.auth.service.system.admin.mapper.admin.user.UserDeptMapper;
import com.auth.service.system.admin.model.po.user.UserPrimaryDeptPO;
import com.auth.service.system.admin.model.vo.authorization.UserAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.user.SysUserDetailVO;
import com.auth.service.system.admin.service.authorization.query.UserAuthorizationSurfaceService;
import com.auth.service.system.common.service.AuditUserDisplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * 用户详情组装
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserDetailSupport {

	private final AuditUserDisplayService auditUserDisplayService;

	private final UserAuthorizationSurfaceService userAuthorizationSurfaceService;

	private final UserReferenceChecker userReferenceChecker;

	private final UserDeptMapper userDeptMapper;

	/**
	 * 按用户 ID 组装详情
	 * @param userId 用户 ID
	 * @return 用户详情
	 */
	public SysUserDetailVO getDetail(Long userId) {
		UserEntity user = userReferenceChecker.getExistingActive(userId);
		SysUserDetailVO detail = SysUserConverter.INSTANCE.toDetailVo(user);

		UserPrimaryDeptPO primaryDept = userDeptMapper.selectPrimaryDeptByUserId(userId);
		if (primaryDept != null) {
			detail.setPrimaryDeptId(primaryDept.getDeptId());
			detail.setPrimaryDeptName(primaryDept.getDeptName());
		}

		UserAuthorizationSummaryVO summary = userAuthorizationSurfaceService.getAuthorizationSummary(userId);
		detail.setDeptCount(summary.getDeptCount());
		detail.setPostCount(summary.getPostCount());
		detail.setDirectRoleCount(summary.getDirectRoleCount());
		detail.setEffectiveRoleCount(summary.getEffectiveRoleCount());
		detail.setEffectivePermissionCount(summary.getEffectivePermissionCount());

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
