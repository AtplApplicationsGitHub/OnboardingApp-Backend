package com.empOnboarding.api.controller;

import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.dto.AppAuthenticationResponse;
import com.empOnboarding.api.dto.SignInRequest;
import com.empOnboarding.api.dto.UserPrincipalDTO;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.TokenProvider;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.Constants;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/auth")
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST }, allowedHeaders = "*")
public class AuthController {

	private final UsersRepository usersRepository;

	private final TokenProvider tokenProvider;

	private final PasswordEncoder passwordEncoder;


	public AuthController(UsersRepository usersRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder) {
		this.usersRepository = usersRepository;
		this.tokenProvider = tokenProvider;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * Testing API method
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/testApi")
	public JSONObject isServiceRunning() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("result", "Employee Onboarding Backend Service is running!");
		jsonObject.put("Current Time", new Date().toString());
		System.gc();
		return jsonObject;
	}

	@PostMapping("/signin")
	public ResponseEntity<AppAuthenticationResponse> signin(@RequestBody SignInRequest loginRequest,
			HttpServletRequest request) throws Exception {
		AppAuthenticationResponse response = new AppAuthenticationResponse(false, "");
		Optional<Users> user = usersRepository.findByNameOrEmail(loginRequest.getSignInId(),
				loginRequest.getSignInId());
		if (user.isPresent()) {
			if (Constants.Y.equalsIgnoreCase(user.get().getActiveFlag())) {
				String jwt = "";
				if (passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
					jwt = tokenProvider.generateToken(user.get().getId());
					response.setAccessToken(jwt);
					response.setUserId(user.get().getId().toString());
					response.setSuccess(Constants.SUCCESS.isStatus());
					response.setMessage(Constants.LOGIN_SUCCESS.getValue());
					request.getSession().setAttribute("authToken", jwt);
					request.getSession().setAttribute("id", user.get().getId().toString());
				} else {
					response.setMessage(Constants.INVALID_PASSWORD.getValue());
				}
			} else {
				response.setMessage(Constants.INVALID_USER.getValue());
			}
		} else {
			response.setMessage(Constants.INVALID_USER.getValue());
		}
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@GetMapping("/loadCurrentUserDetails")
	public UserPrincipalDTO loadCurrentUserDetails(@ApiIgnore @CurrentUser UserPrincipal user) {
		return new UserPrincipalDTO(user.getId(), user.getUsername(),user.getEmail(), user.getRoleName(), user.getPassword());
	}

	@PostMapping("/checkSession")
	public boolean checkSession(HttpServletRequest request, @CurrentUser UserPrincipal currentUser) {
		try {
			request.getSession().invalidate();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
