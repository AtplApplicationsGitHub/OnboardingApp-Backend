package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;

public interface LocationRepository extends JpaRepository<Location, Long> {}