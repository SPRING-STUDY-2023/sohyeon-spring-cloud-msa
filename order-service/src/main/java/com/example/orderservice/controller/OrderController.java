package com.example.orderservice.controller;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.jpa.OrderEntity;
import com.example.orderservice.message_queue.KafkaProducer;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.vo.RequestOrder;
import com.example.orderservice.vo.ResponseOrder;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-service")
public class OrderController {
	private final OrderService orderService;
	private final KafkaProducer kafkaProducer;

	@GetMapping("/health_check")
	public String status(HttpServletRequest request) {
		return String.format("It's working in Order Service on Port %s", request.getServerPort());
	}

	@PostMapping("/{userId}/orders")
	public ResponseEntity<ResponseOrder> createOrder(@PathVariable String userId, @RequestBody RequestOrder orderDetails) {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		/* JPA */
		OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
		orderDto.setUserId(userId);
		OrderDto createdOrder = orderService.createOrder(orderDto);

		ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

		/* send this order to the kafka */
		kafkaProducer.send("example-catalog-topic", orderDto);

		return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
	}

	@GetMapping("/{userId}/orders")
	public ResponseEntity<List<ResponseOrder>> getOrders(@PathVariable String userId) {
		Iterable<OrderEntity> orderList = orderService.getOrdersByUserId(userId);

		List<ResponseOrder> result = new ArrayList<>();
		orderList.forEach(o -> result.add(new ModelMapper().map(o, ResponseOrder.class)));

		return ResponseEntity.ok(result);
	}
}
