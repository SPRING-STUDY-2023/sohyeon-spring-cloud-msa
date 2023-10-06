package com.example.userservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.userservice.config.Greeting;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {

	private final Greeting greeting;

	@GetMapping("/health_check")
	public String status() {
		return "It's working in User Service.";
	}

	@GetMapping("/welcome")
	public String welcome() {
		return greeting.getMessage();
	}
}
