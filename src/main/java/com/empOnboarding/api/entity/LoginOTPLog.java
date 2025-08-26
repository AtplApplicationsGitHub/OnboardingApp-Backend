package com.empOnboarding.api.entity;

import java.util.Date;
import java.util.UUID;

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

import org.hibernate.annotations.Type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "login_otp_log")
@AllArgsConstructor
@NoArgsConstructor
public class LoginOTPLog {

	@Id
	@GeneratedValue(generator = "UUID")
	@Type(type = "org.hibernate.type.UUIDCharType")
	@Column(name = "id", columnDefinition = "VARCHAR(255)")
	private UUID id;

	@Column(name = "otp")
	private String otp;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = true, referencedColumnName = "id")
	private Employee empId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", nullable = false, length = 19)
	private Date createdTime;

}