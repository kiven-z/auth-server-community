package com.auth.service.system.admin.convert.spreadsheet;

import com.auth.common.mapstruct.config.AuthMapperConfig;
import com.auth.common.mapstruct.qualifier.MapStructExportQualifiers;
import com.auth.service.system.admin.excel.post.SysPostSpreadsheetRow;
import com.auth.service.system.admin.model.po.post.SysPostPageRowPO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 岗位表格行转换器
 *
 * @author Bunny
 */
@Mapper(config = AuthMapperConfig.class, uses = MapStructExportQualifiers.class)
public interface SysPostSpreadsheetConverter {

	SysPostSpreadsheetConverter INSTANCE = Mappers.getMapper(SysPostSpreadsheetConverter.class);

	/**
	 * 分页 PO 列表 → Excel 行
	 * @param rows 持久层投影
	 * @return 表格行
	 */
	List<SysPostSpreadsheetRow> toSpreadsheetRows(List<SysPostPageRowPO> rows);

	/**
	 * 分页 PO → Excel 行
	 * @param po 持久层投影
	 * @return 表格行
	 */
	@Mapping(target = "statusLabel", source = "status", qualifiedByName = "statusLabel")
	@Mapping(target = "effectiveLabel", source = "effective", qualifiedByName = "effectiveLabel")
	@Mapping(target = "createdAtText", source = "createdAt", qualifiedByName = "dateTimeText")
	@Mapping(target = "updatedAtText", source = "updatedAt", qualifiedByName = "dateTimeText")
	SysPostSpreadsheetRow toSpreadsheetRow(SysPostPageRowPO po);

}
