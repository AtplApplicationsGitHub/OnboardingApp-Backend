package com.empOnboarding.api.service;

import com.empOnboarding.api.dto.LDAPSettingsDTO;
import com.empOnboarding.api.dto.UsersDTO;
import com.empOnboarding.api.entity.LdapSettingsInfo;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.LDAPSettingRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;


@Service
public class LDAPService {

    @Value("${local.ad.login}")
    private String localADLogin;

    @Value("${local.ad.userList}")
    private String localADUserList;

    final LDAPSettingRepository ldapSettingRepository;

    final MailerService mailerService;

    final UsersRepository usersRepository;

    public LDAPService(LDAPSettingRepository ldapSettingRepository, MailerService mailerService,
                       UsersRepository usersRepository) {
        this.ldapSettingRepository = ldapSettingRepository;
        this.mailerService = mailerService;
        this.usersRepository = usersRepository;
    }

    public JSONObject loadUserFromAD(final List<String> uesrList) {
        JSONObject json = new JSONObject();
        json.put("result", false);
        try {
            Optional<LdapSettingsInfo> info = ldapSettingRepository.findById(1L);
            if (info.isPresent()) {
                JSONObject inputJSON = new JSONObject();
                inputJSON.put("UserNameList", uesrList);
                inputJSON.put("ClientUserName", info.get().getClientUserName());
                inputJSON.put("ClientPassword",
                        new String(Base64.getDecoder().decode(info.get().getClientPassword())));
                inputJSON.put("HostName", info.get().getHostName());
                String url = localADUserList;
                JSONObject responseJSON = callADPOSTService(url, inputJSON.toJSONString());
                return responseJSON;
            }

        } catch (Exception e) {
            return json;
        }
        return json;
    }

    public LDAPSettingsDTO loadByOrgId() {
        LDAPSettingsDTO dto = null;
        Optional<LdapSettingsInfo> info = ldapSettingRepository.findById(1L);
        if (info.isPresent()) {
            dto = new LDAPSettingsDTO(info.get().getId(), info.get().getHostName(),
                    info.get().getClientUserName(),
                    CommonUtls.isCompletlyEmpty(info.get().getClientPassword()) ? ""
                            : new String(Base64.getDecoder().decode(info.get().getClientPassword())));
        }
        return dto;
    }

    private JSONObject callADPOSTService(final String url, final String data) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            HttpEntity<String> entity = new HttpEntity<>(data, headers);
            ResponseEntity<JSONObject> response = restTemplate.exchange(url, HttpMethod.POST, entity, JSONObject.class);
            return response.getBody();
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put("exceptionCustomMsg", e.getMessage());
            return json;
        }
    }

    public boolean checkLDAPSetting(String orgIdOrUserNameOrEmailId) {
        Long orgId = 0L;
        try {
            if (CommonUtls.isVaildNumber(orgIdOrUserNameOrEmailId)) {
                orgId = Long.valueOf(orgIdOrUserNameOrEmailId);
            } else {
                List<Users> user = usersRepository.findByNameAndActiveFlag(orgIdOrUserNameOrEmailId, Constants.Y);
                user = (user.isEmpty())
                        ? usersRepository.findByEmailAndActiveFlag(orgIdOrUserNameOrEmailId, Constants.Y)
                        : user;
                if (!user.isEmpty()) {
                    if ("Application user".equals(user.get(0).getLoginType())) {
                        return false;
                    }

                }
            }
            Optional<LdapSettingsInfo> info = ldapSettingRepository.findById(1L);
            return info.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public JSONObject loadUsersFromAD(List<String> list) {
        JSONObject result = new JSONObject();
        result.put("successUsers", new ArrayList<UsersDTO>());
        List<UsersDTO> userList = new ArrayList<>();

        try {
            JSONObject json = loadUserFromAD(list);
            if (json.containsKey("exceptionCustomMsg") || json.containsKey("statusCode") || json.containsKey("errorBody")) {
                StringBuilder msg = new StringBuilder();
                if (json.get("exceptionCustomMsg") != null) {
                    msg.append(json.get("exceptionCustomMsg"));
                }
                if (json.get("statusCode") != null) {
                    if (msg.length() > 0) msg.append(" | ");
                    msg.append("status=").append(json.get("statusCode"));
                }
                if (json.get("errorBody") != null && json.get("errorBody")!="") {
                    if (msg.length() > 0) msg.append(" | ");
                    msg.append("body=").append(json.get("errorBody"));
                }
                result.put("message", msg.toString());
                return result; // successUsers is already an empty list
            }

            if (!json.isEmpty()) {
                StringBuilder message = new StringBuilder();

                @SuppressWarnings("unchecked")
                List<String> userNotFoundList = (List<String>) json.get("failureUserList");
                if (userNotFoundList != null && !userNotFoundList.isEmpty()) {
                    message.append("User not found : ").append(userNotFoundList).append("<br>");
                }

                @SuppressWarnings("unchecked")
                List<LinkedHashMap<String, Object>> userJSONList =
                        (List<LinkedHashMap<String, Object>>) json.get("successUserInfoList");

                if (userJSONList != null) {
                    for (LinkedHashMap<String, Object> jsonObject : userJSONList) {
                        UsersDTO user = populateADJSONToUserDTO(jsonObject);
                        if (user != null) {
                            user.setLoginType("LDAP");
                            userList.add(user);
                        } else {
                            // mapping failed for one user: return what we have with a message
                            result.put("successUsers", userList);
                            result.put("message", (message.length() > 0 ? message + " " : "")
                                    + "One or more users could not be mapped");
                            return result;
                        }
                    }
                    userJSONList.clear();
                }

                result.put("successUsers", userList);
                result.put("message", message.toString());
            } else {
                // No payload from LDAP; surface that in your single message field
                result.put("message", "Empty response from LDAP");
            }
        } catch (Exception e) {
            result.put("message", e.getMessage());
            return result;
        }

        return result;
    }


    public UsersDTO populateADJSONToUserDTO(LinkedHashMap<String, Object> json) {
        UsersDTO dto = new UsersDTO();
        try {
            dto.setEmail(CommonUtls.isEmpty(json.getOrDefault("email", "")) ? ""
                    : json.getOrDefault("email", "").toString());
            String firstName = CommonUtls.isEmpty(json.getOrDefault("firstName", "")) ? ""
                    : json.getOrDefault("firstName", "").toString();
            String lastName = CommonUtls.isEmpty(json.getOrDefault("lastName", "")) ? ""
                    : json.getOrDefault("lastName", "").toString();
            dto.setName(firstName+" "+lastName);
        } catch (Exception e) {
            return null;
        }
        return dto;
    }

    public JSONObject validateUserByAD(final String userName, final String password) {
        JSONObject json = new JSONObject();
        json.put("result", false);
        json.put("message", Constants.FAILURE_RESULT_MESSAGE);
        try {
            Optional<LdapSettingsInfo> info = ldapSettingRepository.findById(1L);
            if (info.isPresent()) {
                json = getLoginDataForLocalAD(userName, Base64.getEncoder().encodeToString(password.getBytes()),
                        info.get().getHostName());

                json.put("config", true);
            } else {
                json.put("config", false);
                json.put("result", false);
                json.put("message", "LDAP Setting is not configured or it's in-active");
            }
        } catch (Exception e) {
            return json;
        }
        return json;
    }

    public JSONObject getLoginDataForLocalAD(final String userName, final String password, final String hostName) {
        JSONObject finalJSON = new JSONObject();
        finalJSON.put("result", false);
        finalJSON.put("message", Constants.FAILURE_RESULT_MESSAGE);
        try {
            JSONObject inputJSON = new JSONObject();
            inputJSON.put("UserName", userName);
            inputJSON.put("Password", new String(Base64.getDecoder().decode(password)));
            inputJSON.put("HostName", hostName);

            JSONObject responseJSON = callADPOSTService(localADLogin, inputJSON.toJSONString());
            if (!responseJSON.isEmpty()) {
                finalJSON.put("result", responseJSON.getOrDefault("userAuthenticated", false));
                finalJSON.put("message", responseJSON.getOrDefault("exceptionCustomMsg", ""));
            }

        } catch (Exception e) {
            return finalJSON;
        }
        return finalJSON;
    }



}