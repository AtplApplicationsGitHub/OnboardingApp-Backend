package com.empOnboarding.api.controller;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.LocationDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.LocationService;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/location")
@CrossOrigin
public class LocationController {

    final LocationService locationService;

    public LocationController(LocationService locationService){
        this.locationService = locationService;
    }

    @PostMapping("/saveLocation")
    public boolean saveLocation(@RequestBody LocationDTO lDto, CommonDTO dto,
                             HttpServletRequest request, @CurrentUser UserPrincipal user) {
        try {
            dto.setIpAddress(request.getRemoteAddr());
            dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
            dto.setModule(Constants.LAB);
            return locationService.createLocation(lDto, dto,user);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/findFilteredLocation/{location}/{pageNo}")
    public JSONObject findFilteredGroups(@PathVariable String location, @PathVariable String pageNo) throws Exception {
        return locationService.filteredLocation(pageNo,location);
    }

    @GetMapping("/findById/{id}")
    public LocationDTO findDataById(@PathVariable Long id) {
        return locationService.findById(id);
    }

    @GetMapping("/findByDepartment/{department}")
    public List<String> findByDepartment(@PathVariable String department){
        return locationService.location(department);
    }


}