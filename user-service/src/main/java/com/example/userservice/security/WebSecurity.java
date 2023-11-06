package com.example.userservice.security;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.example.userservice.service.UserService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
	private final UserService userService;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final ObjectPostProcessor<Object> objectPostProcessor;
	private final Environment env;

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(CsrfConfigurer::disable)
			.headers(header -> header.frameOptions(FrameOptionsConfig::disable))
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests
					.requestMatchers(new AntPathRequestMatcher(toH2Console() + "/**")).permitAll()
					// .requestMatchers(new AntPathRequestMatcher("/users/**")).permitAll()
					.requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll()
					.requestMatchers(new AntPathRequestMatcher("/**")).permitAll()
					.requestMatchers(new IpAddressMatcher("127.0.0.1")).permitAll()
			)
			.addFilter(getAuthenticationFilter())
			.build();
	}

	// select pwd from users where email=?
	// db_pwd(encrypted) == input_pwd(encrypted)
	public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
		return auth.build();
	}

	private AuthenticationFilter getAuthenticationFilter() throws Exception {
		return new AuthenticationFilter(
			authenticationManager(new AuthenticationManagerBuilder(objectPostProcessor)),
			userService,
			env
		);
	}
}
