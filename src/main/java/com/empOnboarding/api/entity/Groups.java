package com.empOnboarding.api.entity;

import static javax.persistence.GenerationType.IDENTITY;

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
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class Groups implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "primary_group_lead", referencedColumnName = "id")
	private Users pgLead;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "escalation_group_lead", referencedColumnName = "id")
	private Users egLead;
	
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