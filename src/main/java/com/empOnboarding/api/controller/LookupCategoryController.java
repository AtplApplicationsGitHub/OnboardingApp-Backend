package com.empOnboarding.api.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import com.empOnboarding.api.dto.*;
import com.empOnboarding.api.entity.LookupItems;
import com.empOnboarding.api.repository.LookupItemsRepository;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.AuditTrailService;
import com.empOnboarding.api.service.LookupCategoryService;
import com.empOnboarding.api.service.LookupItemsService;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookup")
@CrossOrigin
public class LookupCategoryController {

	private final LookupCategoryService lookupCatetoryService;

	private final LookupItemsService lookupItemsService;

	private final LookupItemsRepository lookupItemRepo;
	
	final
	AuditTrailService auditTrailService;

	public LookupCategoryController(LookupCategoryService lookupCatetoryService, LookupItemsService lookupItemsService, LookupItemsRepository lookupItemRepo, AuditTrailService auditTrailService) {
		this.lookupCatetoryService = lookupCatetoryService;
		this.lookupItemsService = lookupItemsService;
		this.lookupItemRepo = lookupItemRepo;
		this.auditTrailService = auditTrailService;
	}

	@PostMapping("/insertCategory")
	public ResponseEntity<ApiResponse> insertCategory(@RequestBody LookupCategoryDTO lookUpcategoryDTO,
														@CurrentUser UserPrincipal user, HttpServletRequest request) throws Exception {
		ApiResponse response;
        CommonUtls.populateCommonDto(user, lookUpcategoryDTO);
        lookUpcategoryDTO.setIpAddress(request.getRemoteAddr());
        lookUpcategoryDTO.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
		response = new ApiResponse(lookupCatetoryService.insertCategory(lookUpcategoryDTO),
				"Category Inserted successfully", HttpStatus.OK.toString());

        return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	@GetMapping("/getCategoryByName/{categoryName}")
	public ResponseEntity<Boolean> getCategoryByName(@PathVariable(value = "categoryName") String categoryName)
			throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(lookupCatetoryService.getCategoryByName(categoryName));
    }

	@GetMapping("/getCategoriesList")
	public ResponseEntity<List<LookupCategoryDTO>> getCategories() throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(lookupCatetoryService.getCategoryList());
    }

	@PostMapping("/insertCategoryItem")
	public ResponseEntity<ApiResponse> insertCategoryItem(@RequestBody LookupItemsDTO lookUpItemDTO,
			@CurrentUser UserPrincipal user, HttpServletRequest request) throws Exception {
		ApiResponse response;
        CommonUtls.populateCommonDto(user, lookUpItemDTO);
        lookUpItemDTO.setIpAddress(request.getRemoteAddr());
        lookUpItemDTO.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
        lookUpItemDTO.setModule(Constants.LOOKUP_ITEMS);
        lookUpItemDTO.setModuleId(lookUpItemDTO.getKey());
        response = new ApiResponse(lookupItemsService.insertCategoryItem(lookUpItemDTO),
                "Item Inserted to category successfully", HttpStatus.OK.toString());
        return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/getCategoryItemById/{id}/{pageNo}")
	public JSONObject getCategoryItemById(@PathVariable(value = "id") String categoryId,@PathVariable String pageNo)
			throws Exception {
        return lookupItemsService.getCategoryItemsById(categoryId,pageNo);
    }

	@DeleteMapping("/deleteLookUpItem/{id}")
	public ResponseEntity<ApiResponse> deleteLookUpItem(@PathVariable(value = "id") String id,
			@CurrentUser UserPrincipal user, HttpServletRequest request) throws Exception {
		LookupItems lookupItem = lookupItemRepo.findById(Long.valueOf(id))
				.orElseThrow(() -> new Exception("lookupItem not found on :: " + id));

		CommonDTO dto = new CommonDTO();
		dto.setLoginUserId(user.getId());
		dto.setIpAddress(request.getRemoteAddr());
		dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
		dto.setSystemRemarks("Item :" + lookupItem.getValue() + " " + Constants.DATA_DELETE_SUCCESS.getValue());
		dto.setModule(Constants.LOOKUP_ITEMS);
		dto.setModuleId(lookupItem.getValue());
		lookupItemRepo.delete(lookupItem);
		auditTrailService
		.saveAuditTrail(Constants.DATA_DELETE.getValue(), dto);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(Constants.SUCCESS.isStatus(), "deleted Successfully", HttpStatus.OK.toString()));
	}

	@GetMapping("/getCategoryItemByName/{categoryName}")
	public ResponseEntity<List<DropDownDTO>> getCategoryItemByName(@PathVariable String categoryName) throws Exception {
        return ResponseEntity.status(HttpStatus.OK)
                .body(lookupItemsService.getCategoryByCategoryName(categoryName));
    }
	@PostMapping("/updateLookUpItem")
	public ResponseEntity<ApiResponse> updateLookUpItem(@RequestBody LookupItemsDTO lookUpItemDTO,
			HttpServletRequest request,@CurrentUser UserPrincipal user) throws Exception {
		ApiResponse response;
        CommonUtls.populateCommonDto(user, lookUpItemDTO);
        lookUpItemDTO.setIpAddress(request.getRemoteAddr());
        lookUpItemDTO.setModule(Constants.LOOKUP_ITEMS);
        lookUpItemDTO.setModuleId(lookUpItemDTO.getKey());
        lookUpItemDTO.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
        response = new ApiResponse(lookupItemsService.updatelookUpItem(lookUpItemDTO),
                "Item Inserted to category successfully", HttpStatus.OK.toString());

        return ResponseEntity.status(HttpStatus.OK).body(response);
	}
	
}