package com.empOnboarding.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empOnboarding.api.entity.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
	
	Optional<Users> findByNameOrEmail(String username, String email);
	
	@Query(value ="SELECT u.* FROM users u \r\n" + 
			"WHERE u.active_flag =:y AND u.delete_flag =:n", nativeQuery = true)
	List<Users> loadAllActiveUsers(String y, String n);

	Page<Users> findAllByRoleOrderByCreatedTimeDesc(String role, Pageable page);

	Page<Users> findAllByOrderByCreatedTimeDesc(Pageable pageable);
	
	@Query(value = "SELECT u.* FROM users u " +
		            "WHERE u.active_flag = :activeFlag " +
		            "AND (u.name ILIKE CONCAT('%', :keyword, '%') " +
		            "OR u.email ILIKE CONCAT('%', :keyword, '%') " +
		            "OR u.role ILIKE CONCAT('%', :keyword, '%')) " +
		            "ORDER BY u.created_time DESC",
		    countQuery = "SELECT count(*) FROM users u " +
		                 "WHERE u.active_flag = :activeFlag " +
		                 "AND (u.name ILIKE CONCAT('%', :keyword, '%') " +
		                 "OR u.email ILIKE CONCAT('%', :keyword, '%') " +
		                 "OR u.role ILIKE CONCAT('%', :keyword, '%'))",
		    nativeQuery = true)
		Page<Users> findAllBySearch(@Param("keyword") String keyword,
		                            @Param("activeFlag") String activeFlag,
		                            Pageable pageable);

	
}
