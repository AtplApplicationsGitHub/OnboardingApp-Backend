package com.empOnboarding.api.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Questions;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Questions, Long> {

	Page<Questions> findAllByGroupIdIdOrderByCreatedTimeAsc(Long id, Pageable page);

	@Query("""
    select distinct q
    from Questions q
    join QuestionLevel ql
    join QuestionsDepartment qd
    where ql.level = :level and qd.department.key = :department
""")
	List<Questions> findDistinctByLevelAndDepartment(@Param("level") String level,@Param("department") String department);

	long countByGroupIdId(Long id);

	List<Questions> findAllByGroupIdId(Long groupId);
}