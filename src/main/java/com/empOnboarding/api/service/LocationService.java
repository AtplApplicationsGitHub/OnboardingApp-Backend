package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.EmployeeDTO;
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

    private final ConstantRepository constantRepository;

    public LocationService(LocationRepository locationRepository,AuditTrailService auditTrailService,
                           ConstantRepository constantRepository){
        this.locationRepository = locationRepository;
        this.auditTrailService = auditTrailService;
        this.constantRepository = constantRepository;
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

    public LocationDTO populateLocation(Location l) {
        LocationDTO locationDto = new LocationDTO();
        locationDto.setId(l.getId().toString());
        locationDto.setLocation(l.getLocation());
        Constant c = constantRepository.findByConstant("DateFormat");
        locationDto.setCreatedTime(CommonUtls.datetoString(l.getCreatedTime(),c.getConstantValue()));
        locationDto.setUpdatedTime(CommonUtls.datetoString(l.getUpdatedTime(),c.getConstantValue()));
        return locationDto;
    }

    public JSONObject filteredLocation(String pageNo, String location) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<LocationDTO> list;
        Page<Location> lList;
        if (!CommonUtls.isCompletlyEmpty(location)) {
            lList = locationRepository.findAllByLocationOrderByCreatedTimeDesc(location, pageable);
        } else {
            lList = locationRepository.findAllByOrderByCreatedTimeDesc(pageable);
        }
        list = lList.stream().map(this::populateLocation).collect(Collectors.toList());
        json.put("commonListDto", list);
        json.put("totalElements", lList.getTotalElements());
        return json;
    }

    public LocationDTO findById(Long id) {
        LocationDTO lDTO = null;
        Optional<Location> isLocation = locationRepository.findById(id);
        if (isLocation.isPresent()) {
            lDTO = populateLocation(isLocation.get());
        }
        return lDTO;
    }

}