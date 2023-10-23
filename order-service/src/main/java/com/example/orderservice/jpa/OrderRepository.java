package com.example.orderservice.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	Optional<OrderEntity> findByOrderId(String orderId);
	Iterable<OrderEntity> findByUserId(String userId);
}
