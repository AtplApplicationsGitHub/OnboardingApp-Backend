package com.empOnboarding.api.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.empOnboarding.api.entity.AuditTrail;

@Repository
public interface AuditTrailRepository extends JpaRepository<AuditTrail, UUID> {

	List<AuditTrail> findByUsersIdOrderByIdDesc(Long userId);

	@Query("SELECT a FROM AuditTrail a  WHERE a.createdTime >= :fromDate AND a.createdTime < :toDate\r\n"
			+ " ORDER BY a.createdTime DESC")
	Page<AuditTrail> findByFromDataAndToDate(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate,
			Pageable pageable);

	@Query("SELECT a FROM AuditTrail a  WHERE a.users.name = :userId AND a.createdTime >= :fromDate AND a.createdTime < :toDate\r\n"
			+ " ORDER BY a.id DESC")
	List<AuditTrail> findByUsersIdAndFromDataAndToDate(@Param("userId") Long userId, @Param("fromDate") Date fromDate,
			@Param("toDate") Date toDate);

	List<AuditTrail> findByEvent(String event);

	@Query("SELECT e FROM AuditTrail e WHERE e.event IN :columnData AND e.module IN :module AND e.users.name IN :userName AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByEvents(List<String> columnData, List<String> module,
			@Param("userName") List<String> userName, @Param("fromDate") Date fromDate, @Param("toDate") Date toDate,
			Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.event IN :columnData AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByColumnData(List<String> columnData, Date fromDate, Date toDate, Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.module IN :module AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByModuleData(List<String> module, Date fromDate, Date toDate, Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.users.name IN :userName AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByUserName(@Param("userName") List<String> userName, Date fromDate, Date toDate,
			Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.event IN :columnData AND e.users.name IN :userName AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByEventAndUserName(List<String> columnData, @Param("userName") List<String> userName,
			Date fromDate, Date toDate, Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.module IN :module AND e.users.name IN :userName AND e.createdTime >= :fromDate AND e.createdTime < :toDate\r\n"
			+ " ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByModuleAndUserName(List<String> module, List<String> userName, Date fromDate, Date toDate,
			Pageable pageable);

	@Query("SELECT e FROM AuditTrail e WHERE e.event IN :columnData AND e.module IN :module AND DATE(e.createdTime) BETWEEN :fromDate AND :toDate ORDER BY e.createdTime DESC")
	Page<AuditTrail> findByEventAndModule(List<String> columnData, List<String> module, Date fromDate, Date toDate,
			Pageable pageable);

	@Query(value = "SELECT a.event " + "FROM audit_trail a " + "INNER JOIN ( " + "   SELECT event, MAX(id) AS max_id "
			+ "   FROM audit_trail " + "   GROUP BY event " + ") sub ON a.event = sub.event AND a.id = sub.max_id "
			+ "ORDER BY a.id DESC", nativeQuery = true)
	List<String> loadAllEvents();

	@Query(value = "SELECT module FROM (SELECT module,MAX(id) AS id FROM audit_trail a GROUP BY a.module)t ORDER BY id DESC", nativeQuery = true)
	List<String> loadAllModules();

	@Query("SELECT a FROM AuditTrail a WHERE (a.systemRemarks LIKE %:cage% OR a.moduleId LIKE %:cage%) ORDER BY a.createdTime DESC")
	List<AuditTrail> findAuditHistoryByCage(@Param("cage") String cage);

}