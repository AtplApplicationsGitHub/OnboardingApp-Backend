package com.empOnboarding.api.controller;

import com.empOnboarding.api.service.LDAPService;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/api/ldap")
@RestController
@CrossOrigin
public class LDAPContoller {

    final LDAPService ldapService;

    public LDAPContoller(LDAPService ldapService){
        this.ldapService = ldapService;
    }

    @PostMapping("/loadByOrgId")
    public JSONObject loadByOrgId() throws Exception {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("data", ldapService.loadByOrgId());
            jsonObject.put("result", Constants.SUCCESS_RESULT_MESSAGE);
        } catch (Exception ex) {
            jsonObject.put("result", Constants.FAILURE_RESULT_MESSAGE);
            throw ex;
        }
        return jsonObject;
    }

    @PostMapping("/loadUsersFromAD")
    public JSONObject loadUsersFromAD(@RequestBody List<String> list) {
        return ldapService.loadUsersFromAD(list);
    }
}