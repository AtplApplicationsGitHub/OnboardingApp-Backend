package com.empOnboarding.api.repository;

import com.empOnboarding.api.dto.TaskProjection;
import com.empOnboarding.api.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.empOnboarding.api.entity.Groups;
import org.springframework.data.jpa.repository.Query;

public interface TaskRepository extends JpaRepository<Task, String> {

    Task findTopByIdStartingWithOrderByIdDesc(String prefix);

    Page<Task> findAllByOrderByCreatedTimeDesc(Pageable pageable);

    @Query(
            value = """
    WITH qrows AS (
      SELECT
        e.id                                   AS employeeId,
        e."name"                               AS name,
        e.department                           AS department,
        e."role"                               AS role,
        e."level"                              AS level,
        t.id                                   AS task_id,
        t.created_time                         AS created_time,
        tq.id                                  AS tq_id,
        LOWER(COALESCE(tq.status, ''))         AS tq_status,
        CASE
          WHEN q.complaince_day IS NULL OR q.complaince_day !~ '^[0-9]+$' THEN CAST(NULL AS date)
          ELSE
            /* DATE + INTEGER (days) yields DATE in Postgres */
            e.date_of_joining
            + (CASE WHEN LOWER(q.period) = 'after' THEN 1 ELSE -1 END) * CAST(q.complaince_day AS int)
        END AS compliance_date
      FROM employee e
      JOIN task            t  ON t.employee_id = e.id
      JOIN task_questions  tq ON tq.task_id     = t.id
      JOIN questions       q  ON q.id           = tq.question_id
    )
    SELECT
      r.employeeId                               AS employeeId,
      r.name                                     AS name,
      r.department                               AS department,
      r.role                                     AS role,
      r.level                                    AS level,
      STRING_AGG(DISTINCT CAST(r.task_id AS text), ',')          AS taskIds,
      COUNT(r.tq_id)                                             AS totalQuestions,
      COUNT(*) FILTER (WHERE r.tq_status = 'completed')          AS completedQuestions,
      COUNT(*) FILTER (WHERE r.tq_status <> 'completed')         AS pendingQuestions,
      CASE
        WHEN BOOL_AND(r.tq_status = 'completed') THEN 'Completed'
        WHEN BOOL_OR(
               r.tq_status <> 'completed'
           AND r.compliance_date IS NOT NULL
           AND r.compliance_date < CURRENT_DATE
             ) THEN 'Overdue'
        ELSE 'In Progress'
      END                                                       AS status
    FROM qrows r
    GROUP BY r.employeeId, r.name, r.department, r.role, r.level
    ORDER BY MAX(r.created_time) ASC
    """,
            countQuery = """
    WITH qrows AS (
      SELECT
        e.id                                   AS employeeId,
        e."name"                               AS name,
        e.department                           AS department,
        e."role"                               AS role,
        e."level"                              AS level,
        t.id                                   AS task_id,
        t.created_time                         AS created_time,
        tq.id                                  AS tq_id,
        LOWER(COALESCE(tq.status, ''))         AS tq_status,
        CASE
          WHEN q.complaince_day IS NULL OR q.complaince_day !~ '^[0-9]+$' THEN CAST(NULL AS date)
          ELSE
            e.date_of_joining
            + (CASE WHEN LOWER(q.period) = 'after' THEN 1 ELSE -1 END) * CAST(q.complaince_day AS int)
        END AS compliance_date
      FROM employee e
      JOIN task            t  ON t.employee_id = e.id
      JOIN task_questions  tq ON tq.task_id     = t.id
      JOIN questions       q  ON q.id           = tq.question_id
    )
    SELECT COUNT(*) FROM (
      SELECT 1
      FROM qrows r
      GROUP BY r.employeeId, r.name, r.department, r.role, r.level
    ) sub
    """,
            nativeQuery = true
    )
    Page<TaskProjection> findEmployeeTaskSummaries(Pageable pageable);





}