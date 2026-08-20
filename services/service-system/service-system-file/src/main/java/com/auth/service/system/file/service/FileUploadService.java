package com.auth.service.system.file.service;

import com.auth.module.file.api.model.dto.FileUploadResultDTO;
import com.auth.service.system.file.model.form.FileUploadForm;
import com.auth.service.system.file.model.form.MultipleFileUploadForm;

import java.util.List;

/**
 * 文件上传服务
 *
 * @author Bunny
 */
public interface FileUploadService {

	/**
	 * 上传文件并落库
	 * @param form 上传表单
	 * @return 上传结果
	 */
	FileUploadResultDTO upload(FileUploadForm form);

	/**
	 * 上传多个文件并落库
	 * @param form 上传表单
	 * @return 上传结果
	 */
	List<FileUploadResultDTO> uploadMultiple(MultipleFileUploadForm form);

}
