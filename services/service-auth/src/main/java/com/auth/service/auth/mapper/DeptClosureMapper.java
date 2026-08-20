package com.auth.service.auth.mapper;

import com.auth.service.auth.model.po.scope.DeptClosureDescendantRowPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 部门闭包表只读 Mapper（dept_closure）
 *
 * @author Bunny
 */
@Mapper
public interface DeptClosureMapper {

	/**
	 * 将锚点部门展开为锚点与后代的配对行
	 * @param ancestorDeptIds 锚点部门 ID 集合；空集合时返回空列表
	 * @return 闭包配对行
	 */
	List<DeptClosureDescendantRowPO> selectDescendantRowsByAncestorIds(
			@Param("ancestorDeptIds") Collection<Long> ancestorDeptIds);

}
