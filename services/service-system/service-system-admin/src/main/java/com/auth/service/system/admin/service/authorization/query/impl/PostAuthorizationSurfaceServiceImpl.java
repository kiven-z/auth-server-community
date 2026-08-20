package com.auth.service.system.admin.service.authorization.query.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.authorization.AuthorizationSurfaceConverter;
import com.auth.service.system.admin.mapper.authorization.PostRelationQueryMapper;
import com.auth.service.system.admin.model.po.post.SysPostBoundUserPO;
import com.auth.service.system.admin.model.query.authorization.PostUserPageQuery;
import com.auth.service.system.admin.model.vo.authorization.PostAuthorizationSummaryVO;
import com.auth.service.system.admin.model.vo.reference.ext.PostBoundUserReferenceVO;
import com.auth.service.system.admin.service.authorization.query.PostAuthorizationSurfaceService;
import com.auth.service.system.admin.support.post.PostReferenceChecker;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 岗位授权面只读服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostAuthorizationSurfaceServiceImpl implements PostAuthorizationSurfaceService {

	private final PostReferenceChecker postReferenceChecker;

	private final PostRelationQueryMapper postRelationQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<PostBoundUserReferenceVO> pageUsers(Long postId, PostUserPageQuery query) {
		postReferenceChecker.getExistingActive(postId);
		long total = postRelationQueryMapper.countUsersByPostId(postId, query);

		Page<SysPostBoundUserPO> pageParams = new Page<>(query.getPageIndex(), query.getPageSize(), total, false);
		IPage<SysPostBoundUserPO> page = postRelationQueryMapper.selectUsersByPostIdPage(pageParams, postId, query);

		IPage<PostBoundUserReferenceVO> convert = page
			.convert(AuthorizationSurfaceConverter.INSTANCE::toPostBoundUserReference);
		return PageResponse.of(convert);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostAuthorizationSummaryVO getAuthorizationSummary(Long postId) {
		postReferenceChecker.getExistingActive(postId);

		PostAuthorizationSummaryVO summary = new PostAuthorizationSummaryVO();
		summary.setBoundUserCount(postRelationQueryMapper.countUsersByPostId(postId, null));
		return summary;
	}

}
