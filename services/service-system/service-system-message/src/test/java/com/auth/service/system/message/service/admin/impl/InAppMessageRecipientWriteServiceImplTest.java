package com.auth.service.system.message.service.admin.impl;

import com.auth.service.system.message.mapper.InAppMessageRecipientMapper;
import com.auth.service.system.message.model.entity.InAppMessageRecipientEntity;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link InAppMessageRecipientWriteServiceImpl} 单元测试
 *
 * @author Bunny
 */
@DisplayName("InAppMessageRecipientWriteServiceImpl 收件箱幂等写入")
@ExtendWith(MockitoExtension.class)
class InAppMessageRecipientWriteServiceImplTest {

	@Mock
	private InAppMessageRecipientMapper messageInboxMapper;

	private InAppMessageRecipientWriteServiceImpl writeService;

	@BeforeAll
	static void initMybatisPlusTableInfo() {
		// lambdaQuery 需要实体表元数据
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""),
				InAppMessageRecipientEntity.class);
	}

	@BeforeEach
	void setUp() {
		writeService = new InAppMessageRecipientWriteServiceImpl();
		ReflectionTestUtils.setField(writeService, "baseMapper", messageInboxMapper);
	}

	@Test
	@DisplayName("空列表：不调用 Mapper，返回 0")
	void insertBatch_shouldReturnZeroWhenEmpty() {
		assertThat(writeService.insertBatch(List.of())).isZero();
		verify(messageInboxMapper, never()).insertIgnoreBatch(anyList());
	}

	@Test
	@DisplayName("写入前补齐 id/version，并汇总 INSERT IGNORE 影响行数")
	void insertBatch_shouldPrepareRowsAndReturnInsertedCount() {
		// isRead/isDeleted 由组装层负责；此处只验证自定义批量插入绕过 MP 填充时的 id/version
		InAppMessageRecipientEntity row = new InAppMessageRecipientEntity();
		row.setUserId(1L);
		row.setMessageId(9L);
		when(messageInboxMapper.insertIgnoreBatch(anyList())).thenReturn(1);

		int inserted = writeService.insertBatch(List.of(row));

		assertThat(inserted).isEqualTo(1);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<InAppMessageRecipientEntity>> captor = ArgumentCaptor.forClass(List.class);
		verify(messageInboxMapper).insertIgnoreBatch(captor.capture());
		InAppMessageRecipientEntity prepared = captor.getValue().get(0);
		assertThat(prepared.getId()).isNotNull();
		assertThat(prepared.getVersion()).isZero();
	}

	@Test
	@DisplayName("countByMessageId：按 messageId 走 BaseMapper.selectCount")
	void countByMessageId_shouldCountByMessageId() {
		// lambdaQuery().count() 最终调用 BaseMapper.selectCount
		when(messageInboxMapper.selectCount(any())).thenReturn(7L);

		assertThat(writeService.countByMessageId(3L)).isEqualTo(7);
		verify(messageInboxMapper).selectCount(any());
	}

	@Test
	@DisplayName("countByMessageId：messageId 为空时返回 0，不查库")
	void countByMessageId_shouldReturnZeroWhenMessageIdNull() {
		assertThat(writeService.countByMessageId(null)).isZero();
		verify(messageInboxMapper, never()).selectCount(any());
	}

}
