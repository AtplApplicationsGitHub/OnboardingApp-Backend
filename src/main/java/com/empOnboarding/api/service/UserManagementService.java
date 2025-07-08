package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.UserPrincipalDTO;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class UserManagementService {
	private UsersRepository usersRepository;
	
	private AuditTrailService auditTrailService;
	
	private final PasswordEncoder passwordEncoder;

	public UserManagementService(UsersRepository usersRepository,PasswordEncoder passwordEncoder) {
		this.usersRepository = usersRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	public Boolean createUser(UserPrincipalDTO userDto, CommonDTO dto) throws IOException {
		Users user = new Users(null, userDto.getUsername(), userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()), userDto.getRoleName(), Constants.Y,new Date(), new Date());
		usersRepository.save(user);
		dto.setSystemRemarks(user.toString());
		dto.setModuleId(user.getName());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
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
	
	
}