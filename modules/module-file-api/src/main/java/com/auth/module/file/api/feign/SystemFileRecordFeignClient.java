package com.auth.module.file.api.feign;

import com.auth.common.core.model.response.Result;
import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 调用 service-system 文件记录内部接口
 *
 * @author Bunny
 */
@FeignClient(name = "service-system", contextId = "systemFileRecord", path = "/api/system/file/inner")
public interface SystemFileRecordFeignClient {

	/**
	 * 校验 URL 对应文件是否归属指定用户且匹配业务类型
	 * @param request 归属校验请求
	 * @return 统一响应
	 */
	@PostMapping("/file-records/ownership/verify")
	Result<Void> verifyOwnership(@RequestBody OwnedFileAssertByUrlRequest request);

	/**
	 * 尝试撤销归属用户的活跃文件；未命中时静默跳过
	 * @param request 撤销请求
	 * @return 统一响应
	 */
	@PostMapping("/file-records/revoke")
	Result<Void> revokeOwnedFile(@RequestBody OwnedFileDeleteByUrlRequest request);

}
