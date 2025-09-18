package com.empOnboarding.api.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "employee_feedback")
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeFeedbackArch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "star")
    private String star;

    @Column(name = "feedback")
    private String feedback;

    @Column(name = "completed")
    private String completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private TaskArch taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private EmployeeArch employeeId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = true, length = 19)
    private Date createdTime;

}