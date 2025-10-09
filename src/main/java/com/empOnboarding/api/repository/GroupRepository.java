package com.empOnboarding.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;

public interface GroupRepository extends JpaRepository<Groups, Long> {

	Page<Groups> findAllByOrderByCreatedTimeDesc(Pageable pageable);
}