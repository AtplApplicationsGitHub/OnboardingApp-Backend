package com.empOnboarding.api.repository;

import com.empOnboarding.api.entity.LabLocation;
import com.empOnboarding.api.entity.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LabLocationRepository extends JpaRepository<LabLocation, Long> {}