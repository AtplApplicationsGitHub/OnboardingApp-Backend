package com.empOnboarding.api.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.service.GroupService;

@RestController
@RequestMapping("/api/group")
@CrossOrigin
public class GroupController {

	final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

}