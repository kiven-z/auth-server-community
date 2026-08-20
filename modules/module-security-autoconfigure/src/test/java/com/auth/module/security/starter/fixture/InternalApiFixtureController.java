package com.auth.module.security.starter.fixture;

import com.auth.module.security.autoconfigure.annotation.InternalApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/probe-internal")
@RestController
public class InternalApiFixtureController {

	@InternalApi
	@GetMapping("/x")
	String internal() {
		return "ok";
	}

}
