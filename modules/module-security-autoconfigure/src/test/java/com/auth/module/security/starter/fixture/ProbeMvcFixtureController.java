package com.auth.module.security.starter.fixture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/probe")
@RestController
public class ProbeMvcFixtureController {

	@GetMapping("/public")
	String pub() {
		return "ok";
	}

}
