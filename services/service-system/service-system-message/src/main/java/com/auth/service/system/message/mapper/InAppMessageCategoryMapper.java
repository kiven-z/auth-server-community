package com.auth.service.system.message.mapper;

import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.po.InAppMessageCategoryDetailRowPO;
import com.auth.service.system.message.model.po.InAppMessageCategoryPageRowPO;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 站内信业务分类字典
 *
 * @author Bunny
 */
@Mapper
public interface InAppMessageCategoryMapper extends BaseMapper<InAppMessageCategoryEntity> {

	/**
	 * 管理端扁平全量列表
	 * @param query 筛选条件
	 * @return 扁平行
	 */
	List<InAppMessageCategoryPageRowPO> selectCategoryList(@Param("query") InAppMessageCategoryQuery query);

	/**
	 * 按主键查详情
	 * @param id 分类主键
	 * @return 详情行；不存在为 null
	 */
	InAppMessageCategoryDetailRowPO selectDetailById(@Param("id") Long id);

	/**
	 * 按父级列出分类（parentId=0 为大类）
	 * @param parentId 父级主键；0=大类
	 * @param status 启停等值；null=不限
	 * @return 分类列表
	 */
	List<InAppMessageCategoryEntity> selectByParentId(@Param("parentId") Long parentId,
			@Param("status") Boolean status);

}
