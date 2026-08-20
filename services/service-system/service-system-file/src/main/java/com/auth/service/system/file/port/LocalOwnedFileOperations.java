package com.auth.service.system.file.port;

import com.auth.module.file.api.model.request.OwnedFileAssertByUrlRequest;
import com.auth.module.file.api.model.request.OwnedFileDeleteByUrlRequest;
import com.auth.module.file.api.port.OwnedFileOperations;
import com.auth.service.system.file.service.FileRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 单体部署：进程内委托 {@link FileRecordService}。
 *
 * @author Bunny
 */
@RequiredArgsConstructor
@Service
public class LocalOwnedFileOperations implements OwnedFileOperations {

	private final FileRecordService fileRecordService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void assertOwnedFileUrl(OwnedFileAssertByUrlRequest request) {
		fileRecordService.assertOwnedFileUrl(request);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void tryDeleteOwnedByUrl(OwnedFileDeleteByUrlRequest request) {
		fileRecordService.tryDeleteOwnedByUrl(request);
	}

}
