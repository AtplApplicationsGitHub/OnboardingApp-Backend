package com.empOnboarding.api.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import com.empOnboarding.api.dto.*;
import com.empOnboarding.api.entity.Employee;
import com.empOnboarding.api.entity.LoginOTPLog;
import com.empOnboarding.api.repository.EmployeeRepository;
import com.empOnboarding.api.repository.LoginOTPLogRepository;
import com.empOnboarding.api.service.AuditTrailService;
import com.empOnboarding.api.service.LDAPService;
import com.empOnboarding.api.service.MailerService;
import org.json.simple.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.TokenProvider;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.Constants;

import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value = "/api/auth")
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST}, allowedHeaders = "*")
public class AuthController {

    private final UsersRepository usersRepository;

    private final TokenProvider tokenProvider;

    private final PasswordEncoder passwordEncoder;

    private final EmployeeRepository employeeRepository;

    private final LoginOTPLogRepository loginOTPLogRepository;

    private final MailerService mailerService;

    private final AuditTrailService auditTrailService;

    private final LDAPService ldapService;


    public AuthController(UsersRepository usersRepository, TokenProvider tokenProvider, PasswordEncoder passwordEncoder,
                          EmployeeRepository employeeRepository, LoginOTPLogRepository loginOTPLogRepository,
                          MailerService mailerService, AuditTrailService auditTrailService, LDAPService ldapService) {
        this.usersRepository = usersRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.employeeRepository = employeeRepository;
        this.loginOTPLogRepository = loginOTPLogRepository;
        this.mailerService = mailerService;
        this.auditTrailService = auditTrailService;
        this.ldapService = ldapService;
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
            if (user.get().getLoginType().equalsIgnoreCase("Default")) {
                if (Constants.Y.equalsIgnoreCase(user.get().getActiveFlag())) {
                    String jwt = "";
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
                        jwt = tokenProvider.generateToken(user.get().getId(), "Admin");
                        response.setAccessToken(jwt);
                        response.setUserId(user.get().getId().toString());
                        response.setSuccess(Constants.SUCCESS.isStatus());
                        response.setMessage(Constants.LOGIN_SUCCESS.getValue());
                        request.getSession().setAttribute("authToken", jwt);
                        request.getSession().setAttribute("id", user.get().getId().toString());
                        CommonDTO dto = new CommonDTO();
                        dto.setLoginFullName(user.get().getName());
                        dto.setLoginUserId(user.get().getId());
                        dto.setIpAddress(request.getRemoteAddr());
                        dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
                        dto.setSystemRemarks(response.getMessage());
                        auditTrailService.saveAuditTrail(Constants.USER_LOGIN.getValue(), dto);
                    } else {
                        response.setMessage(Constants.INVALID_PASSWORD.getValue());
                    }
                } else {
                    response.setMessage(Constants.INVALID_USER.getValue());
                }
            } else if (user.get().getLoginType().equalsIgnoreCase("LDAP")) {
                JSONObject json = ldapService.validateUserByAD(user.get().getName(), loginRequest.getPassword());
                if ("true".equals(json.getOrDefault("config", false).toString())) {
                    if ("true".equals(json.getOrDefault("result", false).toString())) {
                        String jwt = "";
                        jwt = tokenProvider.generateToken(user.get().getId(), "Admin");
                        response.setAccessToken(jwt);
                        response.setUserId(user.get().getId().toString());
                        response.setSuccess(Constants.SUCCESS.isStatus());
                        response.setMessage(Constants.LOGIN_SUCCESS.getValue());
                        request.getSession().setAttribute("authToken", jwt);
                        request.getSession().setAttribute("id", user.get().getId().toString());
                        CommonDTO dto = new CommonDTO();
                        dto.setLoginFullName(user.get().getName());
                        dto.setLoginUserId(user.get().getId());
                        dto.setIpAddress(request.getRemoteAddr());
                        dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
                        dto.setSystemRemarks(response.getMessage());
                        auditTrailService.saveAuditTrail(Constants.USER_LOGIN.getValue(), dto);
                    }
                }
            }

        } else {
            response.setMessage(Constants.INVALID_USER.getValue());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/employeeSignIn")
    public ResponseEntity<AppAuthenticationResponse> employeeSignIn(@RequestBody SignInRequest loginRequest,
                                                                    HttpServletRequest request) throws Exception {
        AppAuthenticationResponse response = new AppAuthenticationResponse(false, "");
        Optional<Employee> employee = employeeRepository.findByEmail(loginRequest.getSignInId());
        if (employee.isPresent()) {
            String jwt = "";
            if (verifyTotp(employee.get().getId(), loginRequest.getPassword())) {
                jwt = tokenProvider.generateToken(employee.get().getId(), "Employee");
                response.setAccessToken(jwt);
                response.setSuccess(Constants.SUCCESS.isStatus());
                response.setUserId(employee.get().getId().toString());
                response.setMessage(Constants.LOGIN_SUCCESS.getValue());
                request.getSession().setAttribute("authToken", jwt);
                request.getSession().setAttribute("id", employee.get().getId().toString());
                CommonDTO dto = new CommonDTO();
                dto.setLoginFullName(employee.get().getName());
                dto.setLoginUserId(employee.get().getId());
                dto.setIpAddress(request.getRemoteAddr());
                dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
                dto.setSystemRemarks(response.getMessage());
                auditTrailService.saveAuditTrail(Constants.USER_LOGIN.getValue(), dto);
            } else {
                response.setMessage(Constants.INVALID_PASSWORD.getValue());
            }
        } else {
            response.setMessage(Constants.INVALID_USER.getValue());
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/loadCurrentUserDetails")
    public UserPrincipalDTO loadCurrentUserDetails(@ApiIgnore @CurrentUser UserPrincipal user) {
        return new UserPrincipalDTO(user.getId(), user.getUsername(), user.getEmail(), user.getRoleName(), user.getPassword());
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

    @GetMapping("/sendMailOTP/{email}")
    public boolean sendMailOTP(@PathVariable String email) {
        try {
            Optional<Employee> employee = employeeRepository.findByEmail(email);
            showEmailTotp(employee.get().getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/checkEmpOrAdmin/{email}")
    public String checkEmpOrAdmin(@PathVariable String email) {
        try {
            if (email == null || email.isBlank()) {
                return "Invalid User";
            }
            final String normalizedEmail = email.trim();
            if (usersRepository.findByEmail(normalizedEmail).isPresent()) {
                return "Admin";
            }
            if (employeeRepository.findByEmail(normalizedEmail).isPresent()) {
                return "Employee";
            }
            return "Invalid User";
        } catch (Exception ex) {
            return "Invalid User";
        }
    }


    private void showEmailTotp(Long id) {
        String totp = generateOtp();
        Optional<Employee> employee = employeeRepository.findById(id);
        Optional<LoginOTPLog> lg = loginOTPLogRepository.findByEmpIdId(id);
        LoginOTPLog login = new LoginOTPLog(null, totp, employee.orElse(null),
                new Date());
        lg.ifPresent(loginOTPLog -> loginOTPLogRepository.deleteById(loginOTPLog.getId()));
        loginOTPLogRepository.save(login);
        sendTOTPEmail(totp,employee.get().getEmail());
    }


    private Boolean verifyTotp(Long id, String otp) {
        Optional<LoginOTPLog> enteredOtp = loginOTPLogRepository.findByEmpIdId(id);
        if (otp.equals(enteredOtp.get().getOtp())) {
            return true;
        } else {
            return false;
        }
    }

    private String generateOtp() {
        String saltStr = "";
        try {
            String SALTCHARS = "1234567890";
            StringBuilder random = new StringBuilder();
            SecureRandom rand = new SecureRandom();
            while (random.length() < 6) {
                int index = (int) (rand.nextFloat() * SALTCHARS.length());
                random.append(SALTCHARS.charAt(index));
            }
            saltStr = random.toString();
        } catch (Exception e) {
            mailerService.sendEmailOnException(e);
        }
        return saltStr;
    }

    private void sendTOTPEmail(final String otp, final String email) {
        CompletableFuture.runAsync(() -> {
            try {
                InputStream inputStream2 = new ClassPathResource("EmailTemplates/LoginOtp.html")
                        .getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream2));
                String emailBody;
                StringBuilder stringBuilder = new StringBuilder();
                while ((emailBody = br.readLine()) != null) {
                    stringBuilder.append(emailBody);
                }
                emailBody = stringBuilder.toString();
                emailBody = emailBody.replace("@src", Constants.EMPLOYEE_LOGIN);
                emailBody = emailBody.replace("@otp", otp);
                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO(Constants.EMPLOYEE_LOGIN,
                        email.split(","), null, null, emailBody);
                mailerService.sendHTMLMail(emailDetailsDTO);
            } catch (Exception e) {
                mailerService.sendEmailOnException(e);
            }
        });
    }

}
