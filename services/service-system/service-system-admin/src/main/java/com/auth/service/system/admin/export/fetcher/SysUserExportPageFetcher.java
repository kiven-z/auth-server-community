package com.auth.service.system.admin.export.fetcher;

import com.auth.module.platform.persistence.model.UserEntity;
import com.auth.module.security.contract.bizcode.PlatformBizCodes;
import com.auth.service.system.admin.convert.spreadsheet.SysUserSpreadsheetConverter;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetRow;
import com.auth.service.system.admin.mapper.admin.user.SysUserMapper;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import com.auth.service.system.admin.model.query.user.SysUserPageQuery;
import com.auth.service.system.admin.support.sqlbuild.SysUserPageOrderSqlBuilder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 用户导出分页取数
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Component
public class SysUserExportPageFetcher
		extends AbstractExportPageFetcher<SysUserPageQuery, SysUserPageRowPO, SysUserSpreadsheetRow> {

	private final SysUserMapper sysUserMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String bizType() {
		return PlatformBizCodes.SYS_USER;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<SysUserPageQuery> queryType() {
		return SysUserPageQuery.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected IPage<SysUserPageRowPO> queryPage(SysUserPageQuery query, int pageNum, int pageSize) {
		String orderBySql = SysUserPageOrderSqlBuilder.buildOrderBySql(query.getSort());
		Page<UserEntity> pageParams = new Page<>(pageNum, pageSize);
		return sysUserMapper.selectListByPage(pageParams, query, orderBySql);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected SysUserSpreadsheetRow toSpreadsheetRow(SysUserPageRowPO row) {
		return SysUserSpreadsheetConverter.INSTANCE.toSpreadsheetRow(row);
	}

}
