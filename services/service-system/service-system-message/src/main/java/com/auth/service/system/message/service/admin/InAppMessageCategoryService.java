package com.auth.service.system.message.service.admin;

import com.auth.common.core.model.form.IdsEnableStatusForm;
import com.auth.service.system.message.model.entity.InAppMessageCategoryEntity;
import com.auth.service.system.message.model.form.inapp.InAppMessageCategoryForm;
import com.auth.service.system.message.model.query.InAppMessageCategoryQuery;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryDetailVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryPageVO;
import com.auth.service.system.message.model.vo.inapp.InAppMessageCategoryVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 站内信业务分类服务
 *
 * @author Bunny
 */
public interface InAppMessageCategoryService extends IService<InAppMessageCategoryEntity> {

	/**
	 * 查询分类列表
	 * @param query 筛选条件
	 * @return 扁平列表（含审计用户名）
	 */
	List<InAppMessageCategoryPageVO> listCategories(InAppMessageCategoryQuery query);

	/**
	 * 获取分类详情
	 * @param id 主键
	 * @return 详情（含父级展示字段与审计用户名）
	 */
	InAppMessageCategoryDetailVO getCategoryById(Long id);

	/**
	 * 新增分类
	 * @param form 新增表单
	 */
	void create(InAppMessageCategoryForm form);

	/**
	 * 更新分类
	 * @param form 更新表单
	 */
	void update(InAppMessageCategoryForm form);

	/**
	 * 批量启停分类
	 * @param form 主键列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 批量删除分类（物理删除）
	 * @param ids 主键列表
	 */
	void batchDelete(List<Long> ids);

	/**
	 * 查询大类列表
	 * @param status 启停状态；null 表示不限
	 * @return 大类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listMajors(Boolean status);

	/**
	 * 查询指定大类下小类列表
	 * @param parentId 大类主键
	 * @param status 启停状态；null 表示不限
	 * @return 小类 VO；children 为 null
	 */
	List<InAppMessageCategoryVO> listChildren(Long parentId, Boolean status);

}
