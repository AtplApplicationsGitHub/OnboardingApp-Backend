package com.empOnboarding.api.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SPRING_SESSION", uniqueConstraints = @UniqueConstraint(columnNames = "SESSION_ID"))
public class SpringSession implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "PRIMARY_ID", unique = true, nullable = false, length = 36)
	private String primaryId;

	@Column(name = "SESSION_ID", unique = true, nullable = false, length = 36)
	private String sessionId;

	@Column(name = "CREATION_TIME", nullable = false)
	private long creationTime;

	@Column(name = "LAST_ACCESS_TIME", nullable = false)
	private long lastAccessTime;

	@Column(name = "MAX_INACTIVE_INTERVAL", nullable = false)
	private int maxInactiveInterval;

	@Column(name = "EXPIRY_TIME", nullable = false)
	private long expiryTime;

	@Column(name = "PRINCIPAL_NAME", length = 100)
	private String principalName;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "springSession", cascade = CascadeType.ALL)
	private Set<SpringSessionAttributes> springSessionAttributeses = new HashSet<SpringSessionAttributes>(0);

	public SpringSession(String primaryId) {
		this.primaryId = primaryId;
	}
}
