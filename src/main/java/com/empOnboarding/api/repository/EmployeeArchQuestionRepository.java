package com.empOnboarding.api.repository;
import com.empOnboarding.api.entity.EmployeeQuestionsArch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeArchQuestionRepository extends JpaRepository<EmployeeQuestionsArch, Long> {}