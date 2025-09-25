package com.empOnboarding.api.repository;


import com.empOnboarding.api.entity.EmployeeFeedbackArch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeFeedbackArchRepository extends JpaRepository<EmployeeFeedbackArch, Long> {

    void deleteAllByEmployeeIdId(Long id);
}