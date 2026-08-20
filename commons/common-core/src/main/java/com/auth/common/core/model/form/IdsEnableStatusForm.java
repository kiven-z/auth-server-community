package com.auth.common.core.model.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 批量启停：主键列表 + Boolean 状态
 *
 * @author Bunny
 */
@Getter
@Setter
public class IdsEnableStatusForm implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@NotEmpty(message = "主键ID列表不能为空")
	private List<Long> ids;

	@NotNull(message = "启用状态不能为空")
	private Boolean status;

}
