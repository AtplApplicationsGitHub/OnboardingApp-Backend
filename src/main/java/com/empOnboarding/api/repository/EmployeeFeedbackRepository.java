package com.empOnboarding.api.repository;


import com.empOnboarding.api.entity.EmployeeFeedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Questions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeFeedbackRepository extends JpaRepository<EmployeeFeedback, Long> {


    Optional<EmployeeFeedback> findByTaskId(String taskId);

}