package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.LocationDTO;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.LocationRepository;
import com.empOnboarding.api.security.UserPrincipal;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.UsersDTO;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    private final AuditTrailService auditTrailService;

    public LocationService(LocationRepository locationRepository,AuditTrailService auditTrailService){
        this.locationRepository = locationRepository;
        this.auditTrailService = auditTrailService;
    }

    public Boolean createLocation(LocationDTO lDto, CommonDTO dto, UserPrincipal user) throws IOException {
        Users actor = new Users(user.getId());
        Set<LabLocation> lab = new HashSet<>();
        Location l = new Location(null, lDto.getLocation(),lab,new Date(),
                new Date(),actor,actor);
        if (l.getLab() != null) {
            for (String lab1 : lDto.getLab()) {
                lab.add(new LabLocation(null,lab1,l));
            }
        }
        locationRepository.save(l);
        dto.setSystemRemarks(l.toString());
        dto.setModuleId(l.getLocation());
        auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
        return true;
    }

//    public LocationDTO populateLocation(Users user) {
//        UsersDTO userDto = new UsersDTO();
//        userDto.setId(user.getId().toString());
//        userDto.setName(user.getName());
//        userDto.setEmail(user.getEmail());
//        userDto.setRole(user.getRole());
//        userDto.setActiveFlag(user.getActiveFlag());
//        Constant c = constantRepository.findByConstant("DateFormat");
//        userDto.setCreatedTime(CommonUtls.datetoString(user.getCreatedTime(),c.getConstantValue()));
//        userDto.setUpdatedTime(CommonUtls.datetoString(user.getUpdatedTime(),c.getConstantValue()));
//        return userDto;
//    }

}