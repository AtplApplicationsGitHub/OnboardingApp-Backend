package com.empOnboarding.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Constant;

public interface ConstantRepository extends JpaRepository<Constant, String> {

	public Constant findByConstant(String Constant);
}
