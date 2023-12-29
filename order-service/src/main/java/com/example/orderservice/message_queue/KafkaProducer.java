package com.example.orderservice.message_queue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.orderservice.dto.OrderDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducer {
	private final KafkaTemplate<String, String> kafkaTemplate;

	public OrderDto send(String topic, OrderDto orderDto) {
		ObjectMapper mapper = new ObjectMapper();
		String jsonInString = "";
		try {
			jsonInString = mapper.writeValueAsString(orderDto);
		} catch (JsonProcessingException ex) {
			log.error(ex.getMessage(), ex);
		}

		kafkaTemplate.send(topic, jsonInString);
		log.info("Kafka Producer send data from the Order Microservice: " + orderDto);

		return orderDto;
	}
}