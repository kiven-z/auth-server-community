package com.auth.common.web.utils;

import com.auth.common.core.model.response.Result;
import com.auth.common.web.exception.ResponseWriteException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * ResponseUtil单元测试类
 * <p>
 * Unit test class for ResponseUtil
 */
@ExtendWith(MockitoExtension.class)
class ResponseUtilTest {

	@Mock
	private HttpServletResponse mockResponse;

	private StringWriter stringWriter;

	private PrintWriter printWriter;

	@BeforeEach
	void setUp() {
		// 创建用于捕获输出的StringWriter和PrintWriter
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
	}

	/**
	 * 测试正常响应写入功能
	 * <p>
	 * Test normal response writing functionality
	 */
	@Test
	void testOut_NormalResponse_Success() throws Exception {
		// Arrange
		when(mockResponse.getWriter()).thenReturn(printWriter);
		when(mockResponse.getStatus()).thenReturn(200);

		// 模拟一个简单的Result对象（这里假设Result有基本结构）
		Result<String> mockResult = createMockResult("test data");

		// Act
		ResponseUtil.out(mockResponse, mockResult);

		// Assert
		verify(mockResponse).setContentType("application/json;charset=UTF-8");
		verify(mockResponse).getStatus();
		verify(mockResponse).setStatus(200);

		printWriter.flush(); // 确保内容被写入
		String actualOutput = stringWriter.toString();
		assertTrue(actualOutput.contains("test data"), "Response should contain the test data");
	}

	/**
	 * 测试包含特殊字符（多语言）的响应
	 * <p>
	 * Test response with special characters (multi-language support)
	 */
	@Test
	void testOut_MultiLanguageContent_UTF8Encoding() throws Exception {
		// Arrange
		when(mockResponse.getWriter()).thenReturn(printWriter);
		when(mockResponse.getStatus()).thenReturn(200);

		String chineseContent = "你好世界";
		Result<String> mockResult = createMockResult(chineseContent);

		// Act
		ResponseUtil.out(mockResponse, mockResult);

		// Assert
		verify(mockResponse).setContentType("application/json;charset=UTF-8");
		printWriter.flush();
		String actualOutput = stringWriter.toString();
		assertTrue(actualOutput.contains(chineseContent), "Response should properly encode multi-language content");
	}

	/**
	 * 测试不同HTTP状态码的处理
	 * <p>
	 * Test handling of different HTTP status codes
	 */
	@Test
	void testOut_DifferentHttpStatusCodes() throws Exception {
		// Arrange
		when(mockResponse.getWriter()).thenReturn(printWriter);
		when(mockResponse.getStatus()).thenReturn(404); // 模拟404状态码

		Result<String> mockResult = createMockResult("Not Found");

		// Act
		ResponseUtil.out(mockResponse, mockResult);

		// Assert
		verify(mockResponse).setStatus(404);
		verify(mockResponse).getWriter();
	}

	/**
	 * 测试IO异常处理
	 * <p>
	 * Test IO exception handling
	 */
	@Test
	void testOut_IOException_ExceptionThrown() throws Exception {
		// 创建一个会抛出IOException的PrintWriter
		PrintWriter failingPrintWriter = mock(PrintWriter.class);
		IOException ioException = new IOException("Simulated IO error");

		// 关键：模拟write方法抛出异常，使用any()参数匹配器
		doThrow(ioException).when(failingPrintWriter).write(any(char[].class), anyInt(), anyInt());

		when(mockResponse.getWriter()).thenReturn(failingPrintWriter);
		when(mockResponse.getStatus()).thenReturn(200);

		Result<String> mockResult = createMockResult("test");

		// Act & Assert
		ResponseWriteException exception = assertThrows(ResponseWriteException.class,
				() -> ResponseUtil.out(mockResponse, mockResult), "Expected ResponseWriteException to be thrown");

		assertEquals("Failed to write response.", exception.getMessage());
		assertNotNull(exception.getCause());
		assertEquals(IOException.class, exception.getCause().getClass());
		assertEquals("Simulated IO error", exception.getCause().getMessage());
	}

	/**
	 * 测试ObjectMapper配置（JavaTimeModule注册）
	 * <p>
	 * Test ObjectMapper configuration (JavaTimeModule registration)
	 */
	@Test
	void testOut_ObjectMapperConfiguration() throws Exception {
		// 这个测试验证ObjectMapper是否正确配置了JavaTimeModule
		when(mockResponse.getWriter()).thenReturn(printWriter);
		when(mockResponse.getStatus()).thenReturn(200);

		Result<String> mockResult = createMockResult("test with time");

		// Act
		ResponseUtil.out(mockResponse, mockResult);

		// Verify that the response was written without errors
		verify(mockResponse).getWriter();
		printWriter.flush();
		assertFalse(stringWriter.toString().isEmpty(), "Response should not be empty");
	}

	/**
	 * 创建模拟的Result对象 Create a mock Result object
	 */
	private Result<String> createMockResult(String data) {
		Result<String> result = new Result<>();
		result.setData(data);
		result.setCode(Result.SUCCESS_CODE);
		result.setMessage("success");
		return result;
	}

}
