package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class DropDownDTO {
	
	private Long id;

	private String key;
	
	private String value;
	
	private short displayOrder;

	public DropDownDTO(Long id, String feeCategory) {
		this.id = id;
		this.key = feeCategory;
	}
	
	public DropDownDTO(Long id, String key, String value) {
		this.id = id;
		this.key = key;
		this.value=value;
	}
	
	public DropDownDTO(String key, String value) {
		this.key=key;
		this.value=value;
	}

	public DropDownDTO(String key, String value, Short displayOrder) {
		this.key=key;
		this.value=value;
		this.displayOrder=displayOrder;
	}

}
