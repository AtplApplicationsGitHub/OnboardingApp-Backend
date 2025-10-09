package com.empOnboarding.api.repository;


import com.empOnboarding.api.entity.EmployeeFeedbackArch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeFeedbackArchRepository extends JpaRepository<EmployeeFeedbackArch, Long> {

    Optional<EmployeeFeedbackArch> findByTaskIdId(String id);
}