package com.auth.service.system.admin.service.admin.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.admin.convert.admin.SysDeptConverter;
import com.auth.service.system.admin.mapper.admin.dept.SysDeptMapper;
import com.auth.service.system.admin.mapper.authorization.DeptRelationQueryMapper;
import com.auth.service.system.admin.model.entity.SysDeptEntity;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import com.auth.service.system.admin.model.query.dept.SysDeptListQuery;
import com.auth.service.system.admin.model.query.dept.SysDeptPageQuery;
import com.auth.service.system.admin.model.vo.dept.SysDeptDetailVO;
import com.auth.service.system.admin.model.vo.dept.SysDeptListVO;
import com.auth.service.system.admin.service.admin.SysDeptQueryService;
import com.auth.service.system.admin.support.dept.DeptReferenceChecker;
import com.auth.service.system.common.service.AuditUserDisplayService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 部门只读查询服务实现
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class SysDeptQueryServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptEntity> implements SysDeptQueryService {

	private final AuditUserDisplayService auditUserDisplayService;

	private final DeptReferenceChecker deptReferenceChecker;

	private final DeptRelationQueryMapper deptRelationQueryMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SysDeptListVO> listFlat(SysDeptListQuery query) {
		List<SysDeptPageRowPO> rows = baseMapper.selectListByQuery(query);
		List<SysDeptListVO> list = SysDeptConverter.INSTANCE.toListVoFromPageRowList(rows);

		auditUserDisplayService.enrichAuditUsernames(list, null, null);
		return list;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<SysDeptListVO> pageFlat(SysDeptPageQuery query) {
		Page<SysDeptEntity> pageParams = new Page<>(query.getPageIndex(), query.getPageSize());
		IPage<SysDeptPageRowPO> poPage = baseMapper.selectListByPage(pageParams, query);
		IPage<SysDeptListVO> result = poPage.convert(SysDeptConverter.INSTANCE::toListVoFromPageRow);

		auditUserDisplayService.enrichAuditUsernames(result, null, null);
		return PageResponse.of(result);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SysDeptDetailVO getDetail(Long id) {
		SysDeptEntity entity = deptReferenceChecker.getExistingActive(id);

		SysDeptDetailVO detail = SysDeptConverter.INSTANCE.toDetailVo(entity);
		detail.setEffective(baseMapper.countEffectiveById(id) > 0);
		detail.setBoundUserCount(deptRelationQueryMapper.countUsersByDeptId(id, null));
		detail.setBoundPostCount(deptRelationQueryMapper.countPostsByDeptId(id, null));

		auditUserDisplayService.enrichAuditUsernames(Collections.singletonList(detail), null, null);
		return detail;
	}

}
