package com.empOnboarding.api.repository;
import com.empOnboarding.api.entity.EmployeeQuestionsArch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeArchQuestionRepository extends JpaRepository<EmployeeQuestionsArch, Long> {
    List<Long> findDistinctEmployeeIds();
}