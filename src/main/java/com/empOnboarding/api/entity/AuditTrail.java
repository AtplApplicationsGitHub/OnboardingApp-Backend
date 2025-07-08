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
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_trail")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class AuditTrail implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(generator = "UUID")
	@Type(type = "org.hibernate.type.UUIDCharType")
	@Column(name = "id", columnDefinition = "VARCHAR(255)")
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
	private Users users;

	@Column(name = "event", nullable = false, length = 200)
	private String event;

	@Column(name = "ip_address", nullable = false, length = 50)
	private String ipAddress;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", nullable = false, length = 19)
	private Date createdTime;

	@Column(name = "system_remarks", length = 200)
	private String systemRemarks;

	@Column(name = "user_remarks", length = 200)
	private String userRemarks;

	@Column(name = "browser", length = 50)
	private String browser;

	@Column(name = "unique_doc_code", length = 50)
	private String uniqueDocCode;

	@Column(name = "doc_type", length = 50)
	private String docType;

	@Column(name = "documentId")
	private Long documentId;
	
	@Column(name = "module")
	private String module;
	
	@Column(name = "module_data_id")
	private String moduleId;

}