package com.empOnboarding.api.repository;

import com.empOnboarding.api.dto.TaskProjection;
import com.empOnboarding.api.entity.Task;
import com.empOnboarding.api.entity.TaskQuestions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;
import org.springframework.data.jpa.repository.Query;

public interface TaskQuestionRepository extends JpaRepository<TaskQuestions, Long> {}