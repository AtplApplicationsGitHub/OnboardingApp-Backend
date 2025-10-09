package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.TaskArch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskArchRepository extends JpaRepository<TaskArch, String> {

    TaskArch findTopByIdStartingWithOrderByIdDesc(String prefix);
}