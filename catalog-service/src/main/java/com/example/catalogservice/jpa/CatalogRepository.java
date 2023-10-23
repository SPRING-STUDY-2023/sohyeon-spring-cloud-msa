package com.example.catalogservice.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CatalogRepository extends JpaRepository<CatalogEntity, Long> {
	Optional<CatalogEntity> findByProductId(String productId);
}
