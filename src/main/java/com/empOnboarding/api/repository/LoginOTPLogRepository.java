package com.empOnboarding.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.empOnboarding.api.entity.LoginOTPLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginOTPLogRepository extends JpaRepository<LoginOTPLog, Long> {

	Optional<LoginOTPLog> findByEmpIdId(Long loginUserId);

	void deleteByEmpIdId(Long id);
}