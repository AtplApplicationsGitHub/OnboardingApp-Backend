package com.empOnboarding.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;

public interface GroupRepository extends JpaRepository<Groups, Long> {
}