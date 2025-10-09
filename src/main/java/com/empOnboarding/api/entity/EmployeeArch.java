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
@Table(name = "employee_arch")
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeArch implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "email")
	private String email;

	@Column(name = "name")
	private String name;
	
	@Column(name = "department")
	private String department;
	
	@Column(name = "role")
	private String role;
	
	@Column(name = "level")
	private String level;
	
	@Column(name = "total_experience")
	private String totalExperience;
	
	@Column(name = "past_organization")
	private String pastOrganization;
	
	@Column(name = "lab_allocation")
	private String labAllocation;
	
	@Column(name = "complaince_day")
	private String complainceDay;
	
	@Column(name="date_of_joining")
	private LocalDate date;
	
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

}