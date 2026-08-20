package com.auth.service.system.schedule.service;

import com.auth.common.core.model.form.IdsEnableStatusForm;

/**
 * 定时任务运行态同步服务（批量启停、按分组启停）
 *
 * @author Bunny
 */
public interface SysJobScheduleSyncService {

	/**
	 * 批量启停任务并同步调度
	 * @param form 任务 ID 列表与目标状态
	 */
	void batchUpdateStatus(IdsEnableStatusForm form);

	/**
	 * 按分组批量启停任务并同步调度
	 * @param groupCode 分组编码
	 * @param status true=恢复调度，false=暂停
	 */
	void batchUpdateStatusByGroupCode(String groupCode, Boolean status);

}
