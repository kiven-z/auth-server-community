package com.auth.service.system.admin.convert.spreadsheet;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.permission.SysPermissionSpreadsheetRow;
import com.auth.service.system.admin.model.entity.SysPermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 权限表格行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructExportQualifiers.class)
public interface SysPermissionSpreadsheetConverter {

	SysPermissionSpreadsheetConverter INSTANCE = Mappers.getMapper(SysPermissionSpreadsheetConverter.class);

	/**
	 * 实体列表 → Excel 行
	 * @param entities 权限实体
	 * @return 表格行
	 */
	List<SysPermissionSpreadsheetRow> toSpreadsheetRows(List<SysPermissionEntity> entities);

	/**
	 * 实体 → Excel 行
	 * @param entity 权限实体
	 * @return 表格行
	 */
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createdAtText", source = "createdAt", qualifiedByName = "dateTimeText")
	@Mapping(target = "updatedAtText", source = "updatedAt", qualifiedByName = "dateTimeText")
	SysPermissionSpreadsheetRow toSpreadsheetRow(SysPermissionEntity entity);

}
