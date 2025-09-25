package com.empOnboarding.api.entity;


import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "task_arch")
@AllArgsConstructor
@NoArgsConstructor
public class TaskArch {

    @Id
    @Column(name = "id")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private EmployeeArch employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Groups groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to", referencedColumnName = "id")
    private Users assignedTo;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "taskId", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskQuestionsArch> taskQuestions = new HashSet<>(0);

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_time", nullable = true, length = 19)
    private Date updatedTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_time", nullable = true, length = 19)
    private Date createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by", referencedColumnName = "id")
    private Users updatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "id")
    private Users createdBy;

    @Column(name = "freeze_task")
    private String freezeTask;

}