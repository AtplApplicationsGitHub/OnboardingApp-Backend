package com.empOnboarding.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.empOnboarding.api.entity.LookupItems;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LookupItemsRepository extends JpaRepository<LookupItems, Long> {

	Optional<LookupItems> findByKey(String dietType);

	List<LookupItems> findAllByKeyIn(List<String> itemReq);

	Page<LookupItems> findByLookupCategoryIdOrderByDisplayOrderAsc(Long fromString, Pageable pageable);

	List<LookupItems> findByLookupCategoryNameOrderByDisplayOrderAsc(String name);
}