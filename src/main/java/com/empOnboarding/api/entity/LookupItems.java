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

import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lookup_item")
public class LookupItems  implements java.io.Serializable, Diffable<LookupItems> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private LookupCategory lookupCategory;

	@Column(name = "item_key", nullable = false, length = 100)
	private String key;

	@Column(name = "item_value", length = 200,nullable = true)
	private String value;
	
	@Column(name = "display_order",nullable = true)
	private Short displayOrder;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JsonIgnore
	@JoinColumn(name = "updated_by", nullable = true, referencedColumnName = "id")
	private Users users;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_time", nullable = false)
	private Date updatedTime;
    
	public LookupItems(LookupCategory lookupCategory, String key, Date updatedTime) {
		this.lookupCategory = lookupCategory;
		this.key = key;
		this.updatedTime = updatedTime;
	}

	public LookupItems(LookupCategory lookupCategory, String key, String value, String colorCode, Short displayOrder, String activeFlag,
			Date updatedTime) {
		this.lookupCategory = lookupCategory;
		this.key = key;
		this.value = value;
		this.displayOrder = displayOrder;
		this.updatedTime = updatedTime;
	}

	@Override
	public DiffResult<LookupItems> diff(LookupItems obj) {
		DiffResult<LookupItems> build = new DiffBuilder<LookupItems>(this, obj, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("Item Key ", this.key, obj.key)
				.append("Item Value ", this.value, obj.value)
				.append("Display Order ", this.displayOrder, obj.displayOrder)
				.append("Category Name ", this.lookupCategory.getName(), obj.lookupCategory.getName())
				.build();
		return build;
	}

	@Override
	public String toString() {
		return "Lookup Category = " + lookupCategory.getName() + ", Key = " + key + ", Value = " + value + ", DisplayOrder = "
				+ displayOrder;
	}

	public LookupItems(Long id) {
		this.id = id;
	}
	
	
	

}