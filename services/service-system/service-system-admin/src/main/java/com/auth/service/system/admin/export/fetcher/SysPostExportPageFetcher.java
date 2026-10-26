package com.auth.service.system.admin.export.fetcher;

import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.convert.spreadsheet.SysPostSpreadsheetConverter;
import com.auth.service.system.admin.excel.post.SysPostSpreadsheetRow;
import com.auth.service.system.admin.mapper.admin.post.SysPostMapper;
import com.auth.service.system.admin.model.entity.SysPostEntity;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import com.auth.service.system.admin.model.query.post.SysPostQuery;
import com.auth.service.system.admin.support.sqlbuild.SysPostPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 岗位导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class SysPostExportPageFetcher
		extends AbstractExportPageFetcher<SysPostQuery, SysPostPageRowPO, SysPostSpreadsheetRow> {

	private final SysPostMapper sysPostMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bizType() {
		return PlatformBizCodes.SYS_POST;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SysPostQuery> queryType() {
		return SysPostQuery.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPage<SysPostPageRowPO> queryPage(SysPostQuery query, int pageNum, int pageSize) {
		String orderBySql = SysPostPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysPostEntity> pageParams = new Page<>(pageNum, pageSize);
		return sysPostMapper.selectListByPage(pageParams, query, orderBySql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SysPostSpreadsheetRow toSpreadsheetRow(SysPostPageRowPO row) {
		return SysPostSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
	}

}
