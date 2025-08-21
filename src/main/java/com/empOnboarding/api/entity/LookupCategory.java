package com.empOnboarding.api.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "lookup_category", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class LookupCategory implements java.io.Serializable, Diffable<LookupCategory>  {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Column(name = "name", unique = true, nullable = false, length = 50)
	private String name;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time", nullable = false, length = 19)
	private Date updatedTime;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore 
	@JoinColumn(name = "updated_by", nullable = true, referencedColumnName = "id")
	private Users users;
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "lookupCategory")
	private Set<LookupItems> lookupItems = new HashSet<LookupItems>(0);

	public LookupCategory(String name, Date updatedTime) {
		this.name = name;
		this.updatedTime = updatedTime;
	}

	public LookupCategory(String name,Users user, Date updatedTime, Set<LookupItems> lookupItems) {
		this.name = name;
		this.users = user;
		this.updatedTime = updatedTime;
		this.lookupItems = lookupItems;
	}

	@Override
	public DiffResult<LookupCategory> diff(LookupCategory obj) {
		DiffResult<LookupCategory> build = new DiffBuilder<LookupCategory>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("Category Name ", this.name, obj.name)
				.build();
		return build;
	}

	@Override
	public String toString() {
		return "Category Name = " + name;
	}
	
}