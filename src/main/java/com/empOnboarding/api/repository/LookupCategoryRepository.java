package com.empOnboarding.api.repository;

import java.util.Optional;
import java.util.UUID;

import com.empOnboarding.api.entity.LookupCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookupCategoryRepository extends JpaRepository<LookupCategory, Long> {

	boolean existsByName(String name);

	Optional<LookupCategory> findFirstByName(String categoryName);

}