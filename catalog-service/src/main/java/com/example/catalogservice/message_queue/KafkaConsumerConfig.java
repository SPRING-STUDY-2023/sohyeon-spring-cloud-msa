package com.example.catalogservice.message_queue;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
	@Bean
	public ConsumerFactory<String, String> consumerFactory() { // 접속 정보
		Map<String, Object> properties = new HashMap<>();
		properties.put(BOOTSTRAP_SERVERS_CONFIG, "172.18.0.101:9092"); // kafka server
		properties.put(GROUP_ID_CONFIG, "consumerGroupId"); // consumer group id
		properties.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

		return new DefaultKafkaConsumerFactory<>(properties);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() { // 리스너
		ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory =
			new ConcurrentKafkaListenerContainerFactory<>();
		kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());

		return kafkaListenerContainerFactory;
	}
}
