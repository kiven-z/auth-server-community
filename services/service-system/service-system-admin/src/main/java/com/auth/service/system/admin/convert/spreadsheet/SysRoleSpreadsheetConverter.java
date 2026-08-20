package com.auth.service.system.admin.convert.spreadsheet;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.role.SysRoleSpreadsheetRow;
import com.auth.service.system.admin.model.entity.SysRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 角色表格行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructExportQualifiers.class)
public interface SysRoleSpreadsheetConverter {

	SysRoleSpreadsheetConverter INSTANCE = Mappers.getMapper(SysRoleSpreadsheetConverter.class);

	/**
	 * 实体列表 → Excel 行
	 * @param entities 角色实体
	 * @return 表格行
	 */
	List<SysRoleSpreadsheetRow> toSpreadsheetRows(List<SysRoleEntity> entities);

	/**
	 * 实体 → Excel 行
	 * @param entity 角色实体
	 * @return 表格行
	 */
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "createdAtText", source = "createdAt", qualifiedByName = "dateTimeText")
	@Mapping(target = "updatedAtText", source = "updatedAt", qualifiedByName = "dateTimeText")
	SysRoleSpreadsheetRow toSpreadsheetRow(SysRoleEntity entity);

}
