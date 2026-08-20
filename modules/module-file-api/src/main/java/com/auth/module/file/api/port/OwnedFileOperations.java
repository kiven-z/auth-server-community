package com.auth.module.file.api.port;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;

/**
 * 跨模块文件归属校验与撤销（进程内集成契约）。
 *
 * @author Bunny
 */
public interface OwnedFileOperations {

	/**
	 * 校验 URL 对应文件是否归属指定用户且匹配业务类型。
	 * @param request 归属校验请求
	 */
	void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request);

	/**
	 * 尝试撤销归属用户的活跃文件；未命中时静默跳过。
	 * @param request 撤销请求
	 */
	void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request);

}
