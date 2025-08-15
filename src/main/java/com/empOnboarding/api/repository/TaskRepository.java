package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;

public interface TaskRepository extends JpaRepository<Task, String> {

    Task findTopByIdStartingWithOrderByIdDesc(String prefix);

}