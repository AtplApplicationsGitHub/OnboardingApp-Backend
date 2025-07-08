package com.empOnboarding.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.empOnboarding.api.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
	
	Optional<Users> findByNameOrEmail(String username, String email);
	
	@Query(value ="SELECT u.* FROM users u \r\n" + 
			"WHERE u.active_flag =:y AND u.delete_flag =:n", nativeQuery = true)
	List<Users> loadAllActiveUsers(String y, String n);
	
}
