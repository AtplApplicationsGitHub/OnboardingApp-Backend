package com.empOnboarding.api.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "employee_question_arch")
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeQuestionsArch implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private EmployeeArch employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", referencedColumnName = "id")
    private EQuestions questionId;

    @Column(name = "response")
    private String response;

    @Column(name = "completed_flag")
    private Boolean completedFlag;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = true, length = 19)
    private Date createdTime;


}