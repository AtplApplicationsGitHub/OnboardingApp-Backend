package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Generated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LookupItemsDTO extends CommonDTO{

	private String id;	
	
	private String key;
	
	private String value;
	
	private String image;
	
	private Short displayOrder;
	
	private String updatedTime;
	
	private String categoryId;
	
}