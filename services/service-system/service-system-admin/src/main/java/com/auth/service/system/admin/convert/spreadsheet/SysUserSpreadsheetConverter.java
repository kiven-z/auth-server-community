package com.auth.service.system.admin.convert.spreadsheet;

import com.auth.common.data.model.enums.UserStatus;
import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.user.SysUserSpreadsheetRow;
import com.auth.service.system.admin.model.po.user.SysUserPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 用户表格行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructExportQualifiers.class)
public interface SysUserSpreadsheetConverter {

	SysUserSpreadsheetConverter INSTANCE = Mappers.getMapper(SysUserSpreadsheetConverter.class);

	/**
	 * 账号状态编码 → 导出文案
	 * @param status 状态编码
	 * @return 中文标签
	 */
	@Named("userStatusLabel")
	default String userStatusLabel(Integer status) {
		UserStatus userStatus = status == null ? null : UserStatus.of(status);
		return userStatus != null ? userStatus.getDesc() : "";
	}

	/**
	 * 分页 PO 列表 → Excel 行
	 * @param rows 持久层投影
	 * @return 表格行
	 */
	List<SysUserSpreadsheetRow> toSpreadsheetRows(List<SysUserPageRowPO> rows);

	/**
	 * 分页 PO → Excel 行
	 * @param po 持久层投影
	 * @return 表格行
	 */
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "userStatusLabel")
	@Mapping(target = "createdAtText", source = "createdAt", qualifiedByName = "dateTimeText")
	@Mapping(target = "updatedAtText", source = "updatedAt", qualifiedByName = "dateTimeText")
	SysUserSpreadsheetRow toSpreadsheetRow(SysUserPageRowPO po);

}
