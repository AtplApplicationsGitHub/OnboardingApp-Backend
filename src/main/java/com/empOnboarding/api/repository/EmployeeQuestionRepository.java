package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.EQuestions;
import com.empOnboarding.api.entity.EmployeeQuestions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeQuestionRepository extends JpaRepository<EmployeeQuestions, Long> {

    Page<EmployeeQuestions> findAllByEmployeeIdIdOrderByCreatedTimeDesc(Long id, Pageable pageable);

}