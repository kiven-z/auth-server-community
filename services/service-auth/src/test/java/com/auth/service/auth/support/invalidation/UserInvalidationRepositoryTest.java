package com.auth.service.auth.support.invalidation;

import com.auth.common.core.constants.BatchSizes;
import com.auth.service.auth.mapper.UserMapper;
import com.auth.service.auth.model.po.user.UserInvalidationStatePO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * {@link UserInvalidationRepository} 单元测试
 */
@DisplayName("UserInvalidationRepository 用户失效状态")
@ExtendWith(MockitoExtension.class)
class UserInvalidationRepositoryTest {

	@Mock
	private UserMapper userMapper;

	private UserInvalidationRepository repository;

	@BeforeEach
	void setUp() {
		repository = new UserInvalidationRepository(userMapper);
	}

	@Test
	@DisplayName("用户 ID 超过分片大小时应分批加载失效状态")
	void loadByUserIds_largeInput_shouldQueryInChunks() {
		List<Long> userIds = IntStream.rangeClosed(1, BatchSizes.SIZE_500 * 2 + 200).mapToObj(Long::valueOf).toList();
		when(userMapper.selectInvalidationStatesByUserIds(anyList())).thenAnswer(invocation -> {
			List<Long> chunk = invocation.getArgument(0);
			UserInvalidationStatePO projection = new UserInvalidationStatePO();
			projection.setUserId(chunk.get(0));
			projection.setStatus(1);
			return List.of(projection);
		});

		List<UserInvalidationStatePO> states = repository.loadByUserIds(userIds);

		assertEquals(3, states.size());
		verify(userMapper, times(3)).selectInvalidationStatesByUserIds(anyList());
	}

}
