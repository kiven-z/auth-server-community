package com.auth.service.system.admin.convert.spreadsheet;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.dept.SysDeptSpreadsheetRow;
import com.auth.service.system.admin.model.po.dept.SysDeptPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 部门表格行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructExportQualifiers.class)
public interface SysDeptSpreadsheetConverter {

	SysDeptSpreadsheetConverter INSTANCE = Mappers.getMapper(SysDeptSpreadsheetConverter.class);

	/**
	 * 分页 PO → Excel 行
	 * @param po 持久层投影
	 * @return 表格行
	 */
	@Mapping(target = "deptPath", ignore = true)
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "effectiveLabel", source = "effective", qualifiedByName = "effectiveLabel")
	@Mapping(target = "createdAtText", source = "createdAt", qualifiedByName = "dateTimeText")
	@Mapping(target = "updatedAtText", source = "updatedAt", qualifiedByName = "dateTimeText")
	SysDeptSpreadsheetRow toSpreadsheetRow(SysDeptPageRowPO po);

}
