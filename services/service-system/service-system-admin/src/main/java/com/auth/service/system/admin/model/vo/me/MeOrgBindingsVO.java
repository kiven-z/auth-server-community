package com.auth.service.system.admin.model.vo.me;

import com.auth.service.system.admin.model.vo.reference.ext.UserDeptReferenceVO;
import com.auth.service.system.admin.model.vo.reference.ext.UserPostReferenceVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 当前登录用户组织任职（有效部门与岗位）
 *
 * @author Bunny
 */
@Schema(name = "MeOrgBindingsVO", title = "当前用户组织任职")
@Getter
@Setter
@ToString
public class MeOrgBindingsVO {

	@Schema(title = "有效任职部门列表（主部门优先）")
	private List<UserDeptReferenceVO> depts = new ArrayList<>();

	@Schema(title = "有效任职岗位列表（主岗位优先）")
	private List<UserPostReferenceVO> posts = new ArrayList<>();

}
