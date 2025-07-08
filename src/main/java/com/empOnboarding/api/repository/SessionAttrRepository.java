package com.empOnboarding.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.SpringSession;
import com.empOnboarding.api.entity.SpringSessionAttributes;
import com.empOnboarding.api.entity.SpringSessionAttributesId;

public interface SessionAttrRepository extends JpaRepository<SpringSessionAttributes, SpringSessionAttributesId>{

	List<SpringSessionAttributes> findByIdAttributeName(String attributeName);
	
	List<SpringSessionAttributes> findBySpringSession(SpringSession springSession);
}
