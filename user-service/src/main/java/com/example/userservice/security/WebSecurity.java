package com.example.userservice.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
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

	private static final String[] WHITE_LIST = {
		"/users/**",
		"/",
		"/**"
	};

	@Bean
	protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			.csrf(CsrfConfigurer::disable)
			.authorizeHttpRequests(authorizeRequests ->
				authorizeRequests
					// .requestMatchers(WHITE_LIST).permitAll()
					.requestMatchers(PathRequest.toH2Console()).permitAll()
					.requestMatchers(new IpAddressMatcher("127.0.0.1")).permitAll()
			)
			.addFilter(getAuthenticationFilter())
			.headers(header -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
			.build();
	}

	// select pwd from users where email=?
	// db_pwd(encrypted) == input_pwd(encrypted)
	public AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
		return auth.build();
	}

	private AuthenticationFilter getAuthenticationFilter() throws Exception {
		AuthenticationFilter authenticationFilter = new AuthenticationFilter();
		AuthenticationManagerBuilder builder = new AuthenticationManagerBuilder(objectPostProcessor);
		authenticationFilter.setAuthenticationManager(authenticationManager(builder));
		return authenticationFilter;
	}
}
