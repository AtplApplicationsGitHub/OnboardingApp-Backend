package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class LocationDTO {
	
	private String id;
	private String location;
	private List<String> lab;
	private String createdTime;
	private String updatedTime;
	
}