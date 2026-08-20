package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.ReferenceConverter;
import com.auth.service.system.admin.convert.authorization.AuthorizationSurfaceConverter;
import com.auth.service.system.admin.mapper.authorization.DeptRelationQueryMapper;
import com.auth.service.system.admin.model.po.dept.SysDeptBoundUserPO;
import com.auth.service.system.admin.model.po.reference.PostReferencePO;
import com.auth.service.system.admin.model.query.authorization.DeptPostPageQuery;
import com.auth.service.system.admin.model.query.authorization.DeptUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.DeptAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.PostReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.DeptBoundUserReferenceVO;
import com.auth.service.system.admin.service.authorization.query.DeptAuthorizationSurfaceService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 部门授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DeptAuthorizationSurfaceServiceImpl implements DeptAuthorizationSurfaceService {

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptRelationQueryMapper deptRelationQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<DeptBoundUserReferenceVO> pageUsers(Long deptId, DeptUserPageQuery query) {
		deptReferenceChecker.getExistingActive(deptId);
		long total = deptRelationQueryMapper.countUsersByDeptId(deptId, query);

		Page<SysDeptBoundUserPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<SysDeptBoundUserPO> page = deptRelationQueryMapper.selectUsersByDeptIdPage(pageParams, deptId, query);

		IPage<DeptBoundUserReferenceVO> convert = page
			.convert(AuthorizationSurfaceConverter.INSTANCE::toDeptBoundUserReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<PostReferenceVO> pagePosts(Long deptId, DeptPostPageQuery query) {
		deptReferenceChecker.getExistingActive(deptId);
		long total = deptRelationQueryMapper.countPostsByDeptId(deptId, query);

		Page<PostReferencePO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<PostReferencePO> page = deptRelationQueryMapper.selectPostsByDeptIdPage(pageParams, deptId, query);

		IPage<PostReferenceVO> convert = page.convert(ReferenceConverter.INSTANCE::toPostReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DeptAuthorizationSummaryVO getAuthorizationSummary(Long deptId) {
		deptReferenceChecker.getExistingActive(deptId);

		DeptAuthorizationSummaryVO summary = new DeptAuthorizationSummaryVO();
		summary.setBoundUserCount(deptRelationQueryMapper.countUsersByDeptId(deptId, null));
		summary.setBoundPostCount(deptRelationQueryMapper.countPostsByDeptId(deptId, null));
		return summary;
	}

}
