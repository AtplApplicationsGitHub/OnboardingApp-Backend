package com.empOnboarding.api.repository;

import com.empOnboarding.api.dto.TaskProjection;
import com.empOnboarding.api.entity.Task;
import com.empOnboarding.api.entity.TaskQuestions;
import com.empOnboarding.api.entity.TaskQuestionsArch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;
import org.springframework.data.jpa.repository.Query;

public interface TaskQuestionArchRepository extends JpaRepository<TaskQuestionsArch, Long> {}