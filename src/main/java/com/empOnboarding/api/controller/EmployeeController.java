package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.empOnboarding.api.dto.PdfDTO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.empOnboarding.api.utils.CommonUtls;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.EmployeeService;
import com.empOnboarding.api.utils.Constants;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin
public class EmployeeController {
	
	final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}
	
	@PostMapping("/saveEmployee")
	public boolean saveGroup(@RequestBody EmployeeDTO eDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.EMPLOYEE);
			return employeeService.createEmployee(eDto, dto,user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/updateEmployee")
	public boolean updateGroup(@RequestBody EmployeeDTO eDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.EMPLOYEE);
			return employeeService.updateEmployee(eDto, dto, user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/findFilteredEmployee/{search}/{pageNo}")
	public JSONObject findFilteredGroups(@PathVariable String search,@PathVariable String pageNo) throws Exception {
        return employeeService.filteredEmployees(pageNo,search);
    }
	
	@GetMapping("/findById/{id}")
	public EmployeeDTO findDataById(@PathVariable Long id) {
		return employeeService.findById(id);
	}

	
	@DeleteMapping("/deleteEmployee/{id}")
	public void deleteGroup(@PathVariable Long id, CommonDTO dto) throws Exception {
		employeeService.deleteEmployee(id, dto);
	}

	@PostMapping("/generateAddEmployeeExcel")
	public ResponseEntity<PdfDTO> downloadExcel(CommonDTO dto,@CurrentUser UserPrincipal user, HttpServletRequest request,
												HttpServletResponse response) throws Exception {
		CommonUtls.populateCommonDto(user,dto);
		dto.setIpAddress(request.getRemoteAddr());
		dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
		return ResponseEntity.status(HttpStatus.OK)
				.body(employeeService.generateExcel(dto));
	}

	@PostMapping("/importEmployees")
	public JSONObject extractExcelFile(@RequestParam(value = "file") MultipartFile file,
									   @CurrentUser UserPrincipal user, HttpServletRequest request) throws Exception {
		JSONObject json = new JSONObject();
		try {
			CommonDTO commonDto = new CommonDTO();
			CommonUtls.populateCommonDto(user, commonDto);
			commonDto.setIpAddress(request.getRemoteAddr());
			commonDto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			json = employeeService.readExcelFile(new XSSFWorkbook(file.getInputStream()), commonDto);
		} catch (Exception ex) {
		}
		return json;
	}
}