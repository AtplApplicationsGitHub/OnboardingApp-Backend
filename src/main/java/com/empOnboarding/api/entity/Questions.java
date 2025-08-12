package com.empOnboarding.api.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
@Table(name = "questions")
@AllArgsConstructor
@NoArgsConstructor
public class Questions implements java.io.Serializable {

	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Column(name = "text")
	private String text;
	
	@Column(name = "complaince_day")
	private String complainceDay;
	
	@Column(name = "response")
	private String response;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", referencedColumnName = "id")
	private Groups groupId;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "questionId", cascade = CascadeType.ALL)
	private Set<QuestionLevel> questionLevels = new HashSet<>(0);
	
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