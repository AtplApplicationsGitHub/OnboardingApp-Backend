package com.empOnboarding.api.security;

import com.empOnboarding.api.entity.Employee;
import com.empOnboarding.api.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.UsersRepository;

@Service
public class AppCustomUserDetailsService implements UserDetailsService {

	@Autowired
	UsersRepository usersRepository;

	@Autowired
	EmployeeRepository employeeRepository;

	@Override
	@Transactional
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		Users user = usersRepository.findByNameOrEmail(usernameOrEmail,usernameOrEmail).orElseThrow(
				() -> new UsernameNotFoundException("User not found with username or email : " + usernameOrEmail));
		return UserPrincipal.create(user);
	}

	@Transactional
	public UserDetails loadUserById(Long id) {
		Users user = usersRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));
		return UserPrincipal.create(user);
	}

	@Transactional
	public UserDetails loadEmployeeById(Long id) {
		Employee emp = employeeRepository.findById(id)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));
		return UserPrincipal.createEmp(emp);
	}

	@Transactional
	public Users loadUserByUserId(Long id) {
        return usersRepository.findById(id).orElseThrow(
            () -> new UsernameNotFoundException("User not found with id : " + id)
        );
    }

	@Transactional
	public Employee loadEmployeeByEmpId(Long id) {
		return employeeRepository.findById(id).orElseThrow(
				() -> new UsernameNotFoundException("User not found with id : " + id)
		);
	}

}
