package com.example.userservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final RestTemplate restTemplate;
	private final OrderServiceClient orderServiceClient;
	private final Environment env;
	private final CircuitBreakerFactory circuitBreakerFactory;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = userRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException(username));

		return new User(
			userEntity.getEmail(),
			userEntity.getEncryptedPwd(),
			true,
			true,
			true,
			true,
			new ArrayList<>()
		);
	}

	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));

		userRepository.save(userEntity);

		return mapper.map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = userRepository.findByUserId(userId)
			.orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

		// List<ResponseOrder> orders = new ArrayList<>();

		/* Using as rest template */
		// List<ResponseOrder> orders = getOrdersByRestTemplate(userId);

		/* Using a feign client with Feign exception handling */
		// List<ResponseOrder> orders = getOrdersByFeignClient(userId);

		/* Using a feign client with ErrorDecoder */
		// List<ResponseOrder> orders = orderServiceClient.getOrders(userId);
		log.info("Before call orders microservice");
		CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");
		List<ResponseOrder> orders = circuitbreaker.run(() -> orderServiceClient.getOrders(userId),
			throwable -> new ArrayList<>());
		log.info("After call orders microservice");

		userDto.setOrders(orders);

		return userDto;
	}

	private List<ResponseOrder> getOrdersByFeignClient(String userId) {
		List<ResponseOrder> orders = null;

		/* Feign exception handling */
		try {
			orders = orderServiceClient.getOrders(userId);
		} catch (FeignException ex) {
			log.error(ex.getMessage());
		}

		return orders;
	}

	private List<ResponseOrder> getOrdersByRestTemplate(String userId) {
		String orderUrl = String.format(Objects.requireNonNull(env.getProperty("order_service.url")), userId);
		ResponseEntity<List<ResponseOrder>> ordersResponse = restTemplate.exchange(
			orderUrl,
			HttpMethod.GET,
			null,
			new ParameterizedTypeReference<List<ResponseOrder>>() {
			}
		);
		return ordersResponse.getBody();
	}

	@Override
	public Iterable<UserEntity> getUserByAll() {
		return userRepository.findAll();
	}

	@Override
	public UserDto getUserDetailByEmail(String email) {
		UserEntity userEntity = userRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException(email));

		return new ModelMapper().map(userEntity, UserDto.class);
	}
}
