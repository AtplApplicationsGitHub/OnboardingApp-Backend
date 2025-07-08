package com.empOnboarding.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.SpringSession;

public interface SessionDAO extends JpaRepository<SpringSession, String>{
	
	Optional<SpringSession> findBySessionId(String sessionId);
	
}

