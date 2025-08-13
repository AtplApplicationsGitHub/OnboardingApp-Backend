package com.empOnboarding.api.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.empOnboarding.api.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

	Page<Employee> findAllByOrderByCreatedTimeDesc(Pageable pageable);

	@Query(value = "SELECT e.* " + "FROM employee e " + "WHERE (:keyword IS NULL OR :keyword = '' OR "
			+ " e.name               ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.department         ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.role               ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.level              ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.total_experience   ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.past_organization  ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.lab_allocation     ILIKE CONCAT('%', :keyword, '%') OR "
			+ " e.complaince_day     ILIKE CONCAT('%', :keyword, '%') OR "
			+ " TO_CHAR(e.date_of_joining, 'YYYY-MM-DD') ILIKE CONCAT('%', :keyword, '%') " + ") "
			+ "ORDER BY e.created_time DESC", countQuery = "SELECT COUNT(*) " + "FROM employee e "
					+ "WHERE (:keyword IS NULL OR :keyword = '' OR "
					+ " e.name               ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.department         ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.role               ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.level              ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.total_experience   ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.past_organization  ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.lab_allocation     ILIKE CONCAT('%', :keyword, '%') OR "
					+ " e.complaince_day     ILIKE CONCAT('%', :keyword, '%') OR "
					+ " TO_CHAR(e.date_of_joining, 'YYYY-MM-DD') ILIKE CONCAT('%', :keyword, '%') "
					+ ")", nativeQuery = true)
	Page<Employee> findAllBySearch(@Param("keyword") String keyword, Pageable pageable);

}