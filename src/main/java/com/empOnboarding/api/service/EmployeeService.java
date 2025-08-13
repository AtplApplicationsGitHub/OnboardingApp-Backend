package com.empOnboarding.api.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.entity.Constant;
import com.empOnboarding.api.entity.Employee;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.EmployeeRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class EmployeeService {

	private EmployeeRepository employeeRepositrory;

	private AuditTrailService auditTrailService;

	private ConstantRepository constantRepository;

	private MailerService mailerService;

	public EmployeeService(EmployeeRepository employeeRepositrory, AuditTrailService auditTrailService,
			ConstantRepository constantRepository, MailerService mailerService) {
		this.employeeRepositrory = employeeRepositrory;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
		this.mailerService = mailerService;
	}

	public Boolean createEmployee(EmployeeDTO empDto, CommonDTO dto, UserPrincipal user) throws IOException {
		Employee emp = new Employee(null, empDto.getName(), empDto.getDepartment(), empDto.getRole(),empDto.getLevel(),
				empDto.getTotalExperience(),empDto.getPastOrganization(),empDto.getLabAllocation(),empDto.getComplainceDay(),
				LocalDate.parse(empDto.getDate()),new Date(),new Date(), new Users(user.getId()), new Users(user.getId()));
		employeeRepositrory.save(emp);
		dto.setSystemRemarks(emp.toString());
		dto.setModuleId(emp.getName());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
	}

	public EmployeeDTO populateEmployee(Employee emp) {
		EmployeeDTO eDto = new EmployeeDTO();
		eDto.setId(emp.getId().toString());
		eDto.setName(emp.getName());
		eDto.setDepartment(emp.getDepartment());
		eDto.setRole(emp.getRole());
		eDto.setLevel(emp.getLevel());
		eDto.setTotalExperience(emp.getTotalExperience());
		eDto.setPastOrganization(emp.getPastOrganization());
		eDto.setLabAllocation(emp.getLabAllocation());
		Constant c = constantRepository.findByConstant("DateFormat");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(c.getConstantValue());
		String date = emp.getDate().format(formatter);
		eDto.setDate(date);
		eDto.setCreatedTime(CommonUtls.datetoString(emp.getCreatedTime(), c.getConstantValue()));
		eDto.setUpdatedTime(CommonUtls.datetoString(emp.getUpdatedTime(), c.getConstantValue()));
		return eDto;
	}

	public EmployeeDTO findById(Long id) {
		EmployeeDTO eDTO = null;
		Optional<Employee> isEmployee = employeeRepositrory.findById(id);
		if (isEmployee.isPresent()) {
			eDTO = populateEmployee(isEmployee.get());
		}
		return eDTO;
	}

//	public Boolean updateGroup(GroupsDTO gDto, CommonDTO dto, UserPrincipal userp) throws IOException {
//		Optional<Groups> gOpt = groupRepository.findById(Long.valueOf(gDto.getId()));
//		Boolean result = false;
//		if (!gOpt.isPresent()) {
//			mailerService.sendEmailOnException(null);
//		} else {
//			Groups g = gOpt.get();
//			g.setName(gDto.getName());
//			g.setPgLead(new Users(Long.valueOf(gDto.getPgLead())));
//			g.setEgLead(new Users(Long.valueOf(gDto.getEgLead())));
//			g.setUpdatedTime(new Date());
//			g.setUpdatedBy(new Users(userp.getId()));
//			groupRepository.save(g);
//			dto.setSystemRemarks(g.toString());
//			dto.setModuleId(g.getName());
//			auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
//			result = true;
//		}
//		return result;
//	}

	public void deleteEmployee(Long id, CommonDTO dto) throws Exception {
		try {
			employeeRepositrory.deleteById(id);
			dto.setModuleId("NA");
			dto.setSystemRemarks(Constants.GROUP_DELETE.getValue());
			auditTrailService.saveAuditTrail(Constants.DATA_DELETE.getValue(), dto);
		} catch (Exception e) {
			mailerService.sendEmailOnException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public JSONObject filteredEmployees(String pageNo, String search) {
		JSONObject json = new JSONObject();
		Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
		List<EmployeeDTO> list;
		Page<Employee> empList = null;
		if(!CommonUtls.isCompletlyEmpty(search)) {
			empList = employeeRepositrory.findAllBySearch(search,pageable);
		} else {
			empList = employeeRepositrory.findAllByOrderByCreatedTimeDesc(pageable);
		}
		list = empList.stream().map(this::populateEmployee).collect(Collectors.toList());
		json.put("commonListDto", list);
		json.put("totalElements", empList.getTotalElements());
		return json;
	}

}