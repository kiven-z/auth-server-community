package com.auth.service.system.file.controller.me;

import com.auth.common.core.model.response.Result;
import com.auth.common.data.model.PageResponse;
import com.auth.module.security.contract.api.AuthProfile;
import com.auth.service.system.file.model.query.FileExportTaskPageQuery;
import com.auth.service.system.file.model.vo.FileExportTaskDetailVO;
import com.auth.service.system.file.model.vo.FileExportTaskDownloadLinkVO;
import com.auth.service.system.file.model.vo.FileExportTaskPageVO;
import com.auth.service.system.file.service.FileExportTaskQueryService;
import com.auth.service.system.file.service.FileExportTaskWriteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link PersonalFileExportTaskController} 单元测试
 *
 * @author Bunny
 */
@DisplayName("PersonalFileExportTaskController 个人任务")
@ExtendWith(MockitoExtension.class)
class PersonalFileExportTaskControllerTest {

	@Mock
	private FileExportTaskQueryService fileExportTaskQueryService;

	@Mock
	private FileExportTaskWriteService fileExportTaskWriteService;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("page：按当前用户查询分页")
	void pageDelegatesWithCurrentUser() {
		PersonalFileExportTaskController controller = new PersonalFileExportTaskController(fileExportTaskQueryService,
				fileExportTaskWriteService);
		mockCurrentUser(3001L, "alice");
		FileExportTaskPageQuery query = new FileExportTaskPageQuery();
		PageResponse<FileExportTaskPageVO> expected = PageResponse.of(1L, 10L, 1L, List.of(new FileExportTaskPageVO()));
		when(fileExportTaskQueryService.getPage(query, 3001L)).thenReturn(expected);

		Result<PageResponse<FileExportTaskPageVO>> response = controller.page(query);

		assertThat(response.getData()).isSameAs(expected);
		verify(fileExportTaskQueryService).getPage(query, 3001L);
	}

	@Test
	@DisplayName("detail：按当前用户查询详情")
	void detailDelegatesWithCurrentUser() {
		PersonalFileExportTaskController controller = new PersonalFileExportTaskController(fileExportTaskQueryService,
				fileExportTaskWriteService);
		mockCurrentUser(3002L, "bob");
		FileExportTaskDetailVO expected = new FileExportTaskDetailVO();
		expected.setBizType("SYS_USER");
		when(fileExportTaskQueryService.getDetail(901L, 3002L)).thenReturn(expected);

		Result<FileExportTaskDetailVO> response = controller.detail(901L);

		assertThat(response.getData()).isSameAs(expected);
		verify(fileExportTaskQueryService).getDetail(901L, 3002L);
	}

	@Test
	@DisplayName("cancel：按当前用户取消任务")
	void cancelDelegatesWithCurrentUser() {
		PersonalFileExportTaskController controller = new PersonalFileExportTaskController(fileExportTaskQueryService,
				fileExportTaskWriteService);
		mockCurrentUser(3003L, "carol");

		Result<Void> response = controller.cancel(902L);

		assertThat(response).isNotNull();
		verify(fileExportTaskWriteService).cancelTask(902L, 3003L);
	}

	@Test
	@DisplayName("retry：按当前用户重试任务")
	void retryDelegatesWithCurrentUser() {
		PersonalFileExportTaskController controller = new PersonalFileExportTaskController(fileExportTaskQueryService,
				fileExportTaskWriteService);
		mockCurrentUser(3004L, "dave");

		Result<Void> response = controller.retry(903L);

		assertThat(response).isNotNull();
		verify(fileExportTaskWriteService).retryTask(903L, 3004L);
	}

	@Test
	@DisplayName("downloadLink：按当前用户获取下载链接")
	void downloadLinkDelegatesWithCurrentUser() {
		PersonalFileExportTaskController controller = new PersonalFileExportTaskController(fileExportTaskQueryService,
				fileExportTaskWriteService);
		mockCurrentUser(3005L, "eric");
		FileExportTaskDownloadLinkVO expected = new FileExportTaskDownloadLinkVO();
		expected.setUrl("https://example.local/download");
		when(fileExportTaskWriteService.getDownloadLink(904L, 3005L)).thenReturn(expected);

		Result<FileExportTaskDownloadLinkVO> response = controller.downloadLink(904L);

		assertThat(response.getData()).isSameAs(expected);
		verify(fileExportTaskWriteService).getDownloadLink(904L, 3005L);
	}

	private void mockCurrentUser(Long userId, String username) {
		AuthProfile profile = AuthProfile.builder().userId(userId).username(username).build();
		SecurityContextHolder.getContext()
			.setAuthentication(new UsernamePasswordAuthenticationToken(profile, null, null));
	}

}
