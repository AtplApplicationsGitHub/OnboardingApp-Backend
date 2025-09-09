package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.empOnboarding.api.dto.EmployeeFeedbackDTO;
import com.empOnboarding.api.dto.PdfDTO;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import com.empOnboarding.api.utils.CommonUtls;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.EmployeeService;
import com.empOnboarding.api.utils.Constants;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

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
	public ResponseEntity<PdfDTO> downloadExcel(@CurrentUser UserPrincipal user,
												HttpServletRequest request) throws Exception {
		CommonDTO dto = new CommonDTO();
		CommonUtls.populateCommonDto(user,dto);
		dto.setIpAddress(request.getRemoteAddr());
		dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
		return ResponseEntity.status(HttpStatus.OK)
				.body(employeeService.generateExcel(dto));
	}

	@PostMapping(value = "/importEmployees", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<JSONObject> extractExcelFile(
			@RequestParam("file") MultipartFile file,
			@CurrentUser UserPrincipal user,
			HttpServletRequest request) {

		CommonDTO commonDto = new CommonDTO();
		CommonUtls.populateCommonDto(user, commonDto);
		commonDto.setIpAddress(request.getRemoteAddr());
		commonDto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));

		try (InputStream in = file.getInputStream();
			 Workbook workbook = WorkbookFactory.create(in)) {

			JSONObject json = employeeService.readExcelFile(workbook, commonDto,user);
			return ResponseEntity.ok(json);

		} catch (Exception ex) {
			JSONObject err = new JSONObject();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
		}
	}

	@PostMapping("/labSave/{lab}/{empId}")
	public Boolean labSave(@PathVariable String lab,@PathVariable Long empId) throws Exception {
		return employeeService.labSave(lab,empId);
	}

	@PostMapping("saveFeedBack/{star}/{feedback}/{taskId}")
	public Boolean saveFeedBack(@PathVariable String star,@PathVariable String feedback,
								@PathVariable String taskId,@CurrentUser UserPrincipal user) throws Exception {
		return employeeService.saveEmployeeFeedback(star,feedback,taskId,user.getId());
	}

	@GetMapping("getEmployeeFeedBack/{taskId}")
	public EmployeeFeedbackDTO getEmployeeFeedBack(@PathVariable String taskId,@CurrentUser UserPrincipal user){
		return employeeService.findEmployeeFeedBack(taskId, user.getId());
	}

	@GetMapping("/emailExists/{email}")
	public Boolean emailExists(@PathVariable String email) {
		return employeeService.emailExists(email);
	}

	@GetMapping("getConstant/{constant}")
	public String getConstString(@PathVariable String constant){
		return employeeService.getConstant(constant);
	}

}