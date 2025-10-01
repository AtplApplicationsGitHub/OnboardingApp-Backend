package com.empOnboarding.api.repository;
import com.empOnboarding.api.entity.EmployeeQuestionsArch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmployeeArchQuestionRepository extends JpaRepository<EmployeeQuestionsArch, Long> {

    @Query("SELECT DISTINCT eq.employeeId.id FROM EmployeeQuestionsArch eq")
    List<Long> findDistinctEmployeeIds();

}