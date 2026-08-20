package com.auth.service.example.mapper;

import com.auth.module.security.datapermission.annotation.DataScope;
import com.auth.service.example.model.entity.ExampleOrderEntity;
import com.auth.service.example.model.query.ExampleOrderQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 数据权限演示单 Mapper
 *
 * @author Bunny
 */
@Mapper
public interface ExampleOrderMapper extends BaseMapper<ExampleOrderEntity> {

	/**
	 * 按登录画像分页查询演示单
	 * @param page 分页参数
	 * @param query 查询条件
	 * @return 分页数据
	 */
	@DataScope(alias = "d")
	IPage<ExampleOrderEntity> selectScopedPage(Page<ExampleOrderEntity> page, @Param("query") ExampleOrderQuery query);

}
