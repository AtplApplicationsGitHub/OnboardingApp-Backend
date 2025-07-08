package com.empOnboarding.api.dto;

import java.util.List;

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
public class FilterDTO {
	private String fromDate;
	
	private String toDate;
	
	private List<String> selectedEvent;
	
	private List<String> selectedModule;
	
	private List<String> selectedUser;
	
	private Long userId;
}
