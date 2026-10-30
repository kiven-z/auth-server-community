package com.auth.module.file.api.model.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ExportPageDataDTO} Jackson 往返
 *
 * @author Bunny
 */
@DisplayName("ExportPageDataDTO JSON 往返")
class ExportPageDataDTOTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("反序列化保留 records 与 total（含 transient 字段）")
	void jacksonRoundTripKeepsRecordsAndTotal() throws Exception {
		// 远程分页回调依赖 getter/setter 反序列化；@Builder 缺无参构造会直接失败
		ExportPageDataDTO source = ExportPageDataDTO.builder()
			.records(List.of(Map.of("id", 1, "name", "demo")))
			.total(1L)
			.build();

		String json = objectMapper.writeValueAsString(source);
		ExportPageDataDTO restored = objectMapper.readValue(json, ExportPageDataDTO.class);

		assertThat(restored.getTotal()).isEqualTo(1L);
		assertThat(restored.getRecords()).containsExactly(Map.of("id", 1, "name", "demo"));
	}

}
