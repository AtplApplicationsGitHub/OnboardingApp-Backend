package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.UsersDTO;
import com.empOnboarding.api.entity.Constant;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class UserManagementService {
	private UsersRepository usersRepository;

	private AuditTrailService auditTrailService;

	private final PasswordEncoder passwordEncoder;

	private final MailerService mailerService;
	
	private final ConstantRepository constantRepository;

	public UserManagementService(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
			MailerService mailerService,ConstantRepository constantRepository) {
		this.usersRepository = usersRepository;
		this.passwordEncoder = passwordEncoder;
		this.mailerService = mailerService;
		this.constantRepository = constantRepository;
	}

	public Boolean createUser(UsersDTO userDto, CommonDTO dto) throws IOException {
		Users user = new Users(null, userDto.getName(), userDto.getEmail(),
				passwordEncoder.encode(userDto.getPassword()), userDto.getRole(), Constants.Y, new Date(),
				new Date());
		usersRepository.save(user);
		dto.setSystemRemarks(user.toString());
		dto.setModuleId(user.getName());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
	}

	public Boolean updateUser(UsersDTO userDto, CommonDTO dto) throws IOException {
		Optional<Users> userOpt = usersRepository.findById(Long.valueOf(userDto.getId()));
		Boolean result = false;
		if (!userOpt.isPresent()) {
			mailerService.sendEmailOnException(null);
		} else {
			Users user = userOpt.get();
			if (!CommonUtls.isCompletlyEmpty(userDto.getName())) {
				user.setName(userDto.getName());
			}
			if (!CommonUtls.isCompletlyEmpty(userDto.getRole())) {
				user.setRole(userDto.getRole());
			}
			if (!CommonUtls.isCompletlyEmpty(userDto.getPassword())) {
				user.setPassword(passwordEncoder.encode(userDto.getPassword()));
			}
			usersRepository.save(user);
			dto.setSystemRemarks(user.toString());
			dto.setModuleId(user.getName());
			auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
			result = true;
		}
		return result;
	}

	public void UserInlineSave(Long id, String fieldName, String value, CommonDTO dto) throws Exception {
		String oldValue = "";
		Optional<Users> findById = usersRepository.findById(id);
		if (findById.isPresent()) {
			Users user = findById.get();
			switch (fieldName) {
			case "Name":
				oldValue = user.getName();
				user.setName(value);
				break;
			case "Email":
				oldValue = user.getEmail();
				user.setEmail(value);
				break;
			case "Role":
				oldValue = user.getRole();
				user.setRole(value);
				break;
			}
			usersRepository.save(user);
			dto.setModuleId("User Id :" + id);
			dto.setModule(Constants.USER_MANAGEMENT);
			dto.setSystemRemarks(CommonUtls.getDiffForString(fieldName, oldValue, value));
			auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
		}
	}

	public UsersDTO populateUser(Users user) {
		UsersDTO userDto = new UsersDTO();
		userDto.setId(user.getId().toString());
		userDto.setName(user.getName());
		userDto.setEmail(user.getEmail());
		userDto.setRole(user.getRole());
		userDto.setActiveFlag(user.getActiveFlag());
		Constant c = constantRepository.findByConstant("DateFormat");
		userDto.setCreatedTime(CommonUtls.datetoString(user.getCreatedTime(),c.getConstantValue()));
		userDto.setUpdatedTime(CommonUtls.datetoString(user.getUpdatedTime(),c.getConstantValue()));
		return userDto;
	}

	public UsersDTO findById(Long id) {
		UsersDTO usersDTO = null;
		Optional<Users> isPatient = usersRepository.findById(id);
		if (isPatient.isPresent()) {
			usersDTO = populateUser(isPatient.get());
		}
		return usersDTO;
	}

	@SuppressWarnings("unchecked")
	public JSONObject filteredUsers(String pageNo, String search) {
		JSONObject json = new JSONObject();
		Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
		List<UsersDTO> list;
		Page<Users> userList = null;
		if (!CommonUtls.isCompletlyEmpty(search)) {
			userList = usersRepository.findAllBySearch(search,Constants.Y,pageable);
		} else {
			userList = usersRepository.findAllByOrderByCreatedTimeDesc(pageable);
		}
		list = userList.stream().map(this::populateUser).collect(Collectors.toList());
		json.put("commonListDto", list);
		json.put("totalElements", userList.getTotalElements());
		return json;
	}

}