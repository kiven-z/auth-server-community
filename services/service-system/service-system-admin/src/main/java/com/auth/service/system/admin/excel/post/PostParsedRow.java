package com.auth.service.system.admin.excel.post;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * 岗位导入行解析结果（Extract 阶段产物）。
 *
 * @author Bunny
 */
@Value
@Builder
@Accessors(fluent = true)
public class PostParsedRow {

	String deptCode;

	String postCode;

	String postName;

	String statusLabel;

	Integer orderNum;

	String remark;

}
