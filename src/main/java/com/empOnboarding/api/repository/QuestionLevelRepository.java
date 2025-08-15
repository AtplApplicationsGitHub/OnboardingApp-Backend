package com.empOnboarding.api.repository;


import com.empOnboarding.api.entity.QuestionLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Questions;

import java.util.List;

public interface QuestionLevelRepository extends JpaRepository<QuestionLevel, Long> {

	List<QuestionLevel> findAllByLevel(String level);
}