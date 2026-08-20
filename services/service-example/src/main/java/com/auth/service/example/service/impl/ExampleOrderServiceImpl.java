package com.auth.service.example.service.impl;

import com.auth.common.data.model.PageResponse;
import com.auth.service.example.mapper.ExampleOrderMapper;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.auth.service.example.model.vo.ExampleOrderVO;
import com.auth.service.example.service.ExampleOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

/**
 * 数据权限演示单服务实现
 *
 * @author Bunny
 */
@Service
public class ExampleOrderServiceImpl implements ExampleOrderService {

	private final ExampleOrderMapper exampleOrderMapper;

	public ExampleOrderServiceImpl(ExampleOrderMapper exampleOrderMapper) {
		this.exampleOrderMapper = exampleOrderMapper;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PageResponse<ExampleOrderVO> getPage(ExampleOrderQuery query) {
		Page<ExampleOrderEntity> page = new Page<>(query.getPageIndex(), query.getPageSize());

		IPage<ExampleOrderVO> voPage = exampleOrderMapper.selectScopedPage(page, query).convert(row -> {
			ExampleOrderVO vo = new ExampleOrderVO();
			vo.setId(row.getId());
			vo.setTitle(row.getTitle());
			vo.setDeptId(row.getDeptId());
			vo.setCreatedBy(row.getCreatedBy());
			return vo;
		});
		return PageResponse.of(voPage);
	}

}
