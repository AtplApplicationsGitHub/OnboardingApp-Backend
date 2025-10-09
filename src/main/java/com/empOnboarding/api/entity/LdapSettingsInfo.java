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

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ldap_settings_info")
public class LdapSettingsInfo implements java.io.Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "id", unique = true, nullable = false)
	@GeneratedValue(strategy = IDENTITY)
	private long id;
	
	@Column(name = "host_name", nullable = true, length = 50)
	private String hostName;
	
	@Column(name = "client_user_name", nullable = true, length = 100)
	private String clientUserName;
	
	@Column(name = "client_password", nullable = true, length = 100)
	private String clientPassword;

}