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
@Table(name = "level_questions")
@AllArgsConstructor
@NoArgsConstructor
public class QuestionLevel implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "level")
	private String level;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "question_id", referencedColumnName = "id")
	private Questions questionId;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_time", nullable = true, length = 19)
	private Date createdTime;
}