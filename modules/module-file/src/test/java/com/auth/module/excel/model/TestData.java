package com.auth.module.excel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(title = "测试数据")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestData {

	@ExcelProperty(value = "学院")
	private String college;

	@ExcelProperty(value = "班级")
	private String className;

	@ExcelProperty(value = "学号")
	private String studentId;

	@ExcelProperty(value = "姓名")
	private String name;

	@ExcelProperty(value = "等第")
	private String grade;

}
