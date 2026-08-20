package com.auth.common.web.resttemplate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link RestTemplateUtil} unit tests.
 */
@DisplayName("RestTemplateUtil HTTP exchange")
@ExtendWith(MockitoExtension.class)
class RestTemplateUtilTest {

	@Mock
	private RestTemplate restTemplate;

	@Test
	@DisplayName("exchange：应组装 headers 与 body，并默认 Accept JSON")
	void exchange_appliesHeadersAndBody() {
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.POST),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("ok"));

		ResponseEntity<String> response = RestTemplateUtil.exchange(restTemplate, "https://example.com/api",
				HttpMethod.POST, Map.of("X-Token", "abc"), "{\"id\":1}");

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.POST), entityCaptor.capture(),
				eq(String.class));
		HttpEntity<String> request = entityCaptor.getValue();
		assertThat(request.getHeaders().getFirst("X-Token")).isEqualTo("abc");
		assertThat(request.getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);
		assertThat(request.getBody()).isEqualTo("{\"id\":1}");
		assertThat(response.getBody()).isEqualTo("ok");
	}

	@Test
	@DisplayName("exchange：headers 为 null 时应默认 Accept JSON 并携带 body")
	void exchange_whenHeadersNull_defaultsAcceptJson() {
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.PUT),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("updated"));

		RestTemplateUtil.exchange(restTemplate, "https://example.com/api", HttpMethod.PUT, null, "payload");

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.PUT), entityCaptor.capture(),
				eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getAccept()).containsExactly(MediaType.APPLICATION_JSON);
		assertThat(entityCaptor.getValue().getBody()).isEqualTo("payload");
	}

	@Test
	@DisplayName("exchange：已配置 Accept 时不应覆盖")
	void exchange_whenAcceptPresent_keepsOriginal() {
		when(restTemplate.exchange(eq("https://example.com/api"), eq(HttpMethod.GET),
				org.mockito.ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(ResponseEntity.ok("plain"));

		RestTemplateUtil.exchange(restTemplate, "https://example.com/api", HttpMethod.GET,
				Map.of(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE), null);

		ArgumentCaptor<HttpEntity<String>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		verify(restTemplate).exchange(eq("https://example.com/api"), eq(HttpMethod.GET), entityCaptor.capture(),
				eq(String.class));
		assertThat(entityCaptor.getValue().getHeaders().getAccept()).containsExactly(MediaType.TEXT_PLAIN);
	}

}
