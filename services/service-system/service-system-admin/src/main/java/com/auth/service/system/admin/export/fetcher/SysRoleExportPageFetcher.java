package com.auth.service.system.admin.export.fetcher;

import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.convert.spreadsheet.SysRoleSpreadsheetConverter;
import com.auth.service.system.admin.excel.role.SysRoleSpreadsheetRow;
import com.auth.service.system.admin.mapper.admin.role.SysRoleMapper;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import com.auth.service.system.admin.model.query.role.SysRoleQuery;
import com.auth.service.system.admin.support.sqlbuild.SysRolePageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 角色导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class SysRoleExportPageFetcher
		extends AbstractExportPageFetcher<SysRoleQuery, SysRoleEntity, SysRoleSpreadsheetRow> {

	private final SysRoleMapper sysRoleMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bizType() {
		return PlatformBizCodes.SYS_ROLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SysRoleQuery> queryType() {
		return SysRoleQuery.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPage<SysRoleEntity> queryPage(SysRoleQuery query, int pageNum, int pageSize) {
		String orderBySql = SysRolePageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<SysRoleEntity> pageParams = new Page<>(pageNum, pageSize);
		return sysRoleMapper.selectListByPage(pageParams, query, orderBySql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SysRoleSpreadsheetRow toSpreadsheetRow(SysRoleEntity row) {
		return SysRoleSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
	}

}
