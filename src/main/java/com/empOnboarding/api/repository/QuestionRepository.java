package com.empOnboarding.api.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Questions;

public interface QuestionRepository extends JpaRepository<Questions, Long> {

	Page<Questions> findAllByGroupIdIdOrderByCreatedTimeAsc(Long id, Pageable page);

	long countByGroupIdId(Long id);
	
}