package com.auth.service.system.file.service;

import com.auth.common.data.model.PageResponse;
import com.auth.service.system.file.model.entity.FileRecordEntity;
import com.auth.service.system.file.model.query.FileRecordPageQuery;
import com.auth.service.system.file.model.vo.FileRecordDetailVO;
import com.auth.service.system.file.model.vo.FileRecordPageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 文件记录查询服务
 *
 * @author Bunny
 */
public interface FileRecordQueryService extends IService<FileRecordEntity> {

	/**
	 * 分页查询文件记录
	 * @param query 查询条件
	 * @param isDeleted 逻辑删除状态
	 * @return 分页结果
	 */
	PageResponse<FileRecordPageVO> getPage(FileRecordPageQuery query, Boolean isDeleted);

	/**
	 * 获取文件记录详情
	 * @param id 文件记录ID
	 * @param ownerUserId 归属用户ID
	 * @param isPrivate 是否私有文件
	 * @param expectedDeleted 是否预期删除
	 * @param deleteSources 删除来源集合，非空时按 IN 过滤
	 * @return 文件记录详情
	 */
	FileRecordDetailVO getDetail(Long id, Long ownerUserId, Boolean isPrivate, boolean expectedDeleted,
			List<String> deleteSources);

}
