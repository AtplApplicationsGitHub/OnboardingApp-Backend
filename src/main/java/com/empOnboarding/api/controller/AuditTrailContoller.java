package com.empOnboarding.api.controller;


import com.empOnboarding.api.dto.AuditTrailDTO;
import com.empOnboarding.api.dto.FilterDTO;
import com.empOnboarding.api.dto.MultiSelectDropDownDTO;
import com.empOnboarding.api.dto.UserMultiSelectDropDown;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.AuditTrailService;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RequestMapping("/api/audit")
@RestController
@CrossOrigin
public class AuditTrailContoller {

	final
	AuditTrailService auditTrailService;

	public AuditTrailContoller(AuditTrailService auditTrailService) {
		this.auditTrailService = auditTrailService;
	}

	@GetMapping("/loadCurrentUserAuditTrail")
	public List<AuditTrailDTO> loadCurrentUserAuditTrail(@ApiIgnore @CurrentUser UserPrincipal user) throws Exception {
        return auditTrailService.loadAuditTrail(user.getId());
    }

	@GetMapping("/loadAllAuditTrail")
	public List<AuditTrailDTO> loadAllAuditTrailDetails() {
        return auditTrailService.loadAllAuditTrail();
    }

	@GetMapping("/loadAllAuditTrailBasedOnDateRange/{pageNo}")
	public List<AuditTrailDTO> loadAllAuditTrailBasedOnDateRange(@RequestParam("fromDate") String from,
			@RequestParam("toDate") String to,@PathVariable(value = "pageNo" )int pageNo)throws Exception {
        return auditTrailService.loadAllAuditTrailBasedOnDateRange(from, to,pageNo);
	}

	@GetMapping("/loadAllAuditTrailBasedEvent")
	public List<AuditTrailDTO> loadAllAuditTrailBasedOnEvent(@RequestParam("Event") String event) {
        return auditTrailService.loadAllAuditTrailBasedOnEvent(event);
    }
	
	@PostMapping("/findFilteredData/{pageNo}")
	public JSONObject findFilteredData(@RequestBody FilterDTO filterDto, @PathVariable(value = "pageNo" ) int pageNo) throws Exception {
        return auditTrailService.findFilteredData(filterDto,pageNo);
    }
	@GetMapping("/getEventByName")
	public ResponseEntity<List<MultiSelectDropDownDTO>> getEventByName() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(auditTrailService.getEventByName());
    }
	
	@GetMapping("/getModuleByName")
	public ResponseEntity<List<MultiSelectDropDownDTO>> getModuleByName() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(auditTrailService.getModuleByName());
    }
	
	@GetMapping("/getUserByName")
	public ResponseEntity<List<MultiSelectDropDownDTO>> getUserByName() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(auditTrailService.getUserByName());
    }
	
	@GetMapping("/getUserDetails")
	public ResponseEntity<List<UserMultiSelectDropDown>> getUserDetails() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(auditTrailService.getUserDropDown());
    }
	
	@PostMapping("/findCageAudit/{pageNo}")
	public JSONObject findFilteredDataCageAudit(@RequestBody String cage,@PathVariable(value = "pageNo" ) int pageNo) throws Exception {
        return auditTrailService.findFilteredDataCageAudit(cage,pageNo);
    }
}