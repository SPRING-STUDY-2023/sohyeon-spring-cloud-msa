package com.example.catalogservice.message_queue;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
	private final CatalogRepository catalogRepository;

	@KafkaListener(topics = "example-catalog-topic")
	@Transactional
	public void updateQty(String kafkaMessage) {
		log.info("Kafka Message: ->" + kafkaMessage);

		Map<Object, Object> map = getResponseMap(kafkaMessage);
		CatalogEntity catalog = findCatalog((String)map.get("productId"));

		catalog.decreaseStock((int)map.get("qty"));
	}

	private Map<Object, Object> getResponseMap(String kafkaMessage) {
		Map<Object, Object> map = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			map = mapper.readValue(kafkaMessage, new TypeReference<>() {
			});
		} catch (JsonProcessingException ex) {
			log.error(ex.getMessage(), ex);
		}

		return map;
	}

	private CatalogEntity findCatalog(String productId) {
		return catalogRepository.findByProductId(productId)
			.orElseThrow(EntityNotFoundException::new);
	}
}
