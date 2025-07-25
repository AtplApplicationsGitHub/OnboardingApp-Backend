package com.empOnboarding.api.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "constant")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class Constant implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "constant_value", unique = true, nullable = false)
	private String constant;

	@Column(name = "value")
	private String constantValue;
}
