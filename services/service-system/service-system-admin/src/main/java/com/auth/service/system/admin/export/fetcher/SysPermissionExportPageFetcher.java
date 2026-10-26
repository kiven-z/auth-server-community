package com.auth.service.system.admin.export.fetcher;

import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.convert.spreadsheet.SysPermissionSpreadsheetConverter;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetRow;
import com.auth.service.system.admin.mapper.admin.permission.SysPermissionMapper;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import com.auth.service.system.admin.model.query.permission.SysPermissionQuery;
import com.auth.service.system.admin.support.sqlbuild.SysPermissionPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 权限导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class SysPermissionExportPageFetcher
		extends AbstractExportPageFetcher<SysPermissionQuery, SysPermissionEntity, SysPermissionSpreadsheetRow> {

	private final SysPermissionMapper sysPermissionMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bizType() {
		return PlatformBizCodes.SYS_PERMISSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SysPermissionQuery> queryType() {
		return SysPermissionQuery.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPage<SysPermissionEntity> queryPage(SysPermissionQuery query, int pageNum, int pageSize) {
		String orderBySql = SysPermissionPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysPermissionEntity> pageParams = new Page<>(pageNum, pageSize);
		return sysPermissionMapper.selectListByPage(pageParams, query, orderBySql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SysPermissionSpreadsheetRow toSpreadsheetRow(SysPermissionEntity row) {
		return SysPermissionSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
	}

}
