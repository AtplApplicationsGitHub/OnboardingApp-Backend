package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.GroupsDTO;
import com.empOnboarding.api.dto.LocationDTO;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.LocationRepository;
import com.empOnboarding.api.repository.LookupItemsRepository;
import com.empOnboarding.api.security.UserPrincipal;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    private final AuditTrailService auditTrailService;

    private final ConstantRepository constantRepository;

    private final LookupItemsRepository lookupItemsRepository;

    private final MailerService mailerService;

    public LocationService(LocationRepository locationRepository,AuditTrailService auditTrailService,
                           ConstantRepository constantRepository,MailerService mailerService,
                           LookupItemsRepository lookupItemsRepository){
        this.locationRepository = locationRepository;
        this.auditTrailService = auditTrailService;
        this.constantRepository = constantRepository;
        this.mailerService = mailerService;
        this.lookupItemsRepository = lookupItemsRepository;
    }

    public Boolean createLocation(LocationDTO lDto, CommonDTO dto, UserPrincipal user) {
        Set<LabLocation> lab = new HashSet<>();
        Location l = new Location(null, lDto.getLocation(),lab,new Date(),
                new Date(),new Users(user.getId()),new Users(user.getId()));
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
        List<String> lab = new ArrayList<>();
        for (LabLocation labs : l.getLab()) {
            lab.add(labs.getLab());
        }
        locationDto.setLab(lab);
        Constant c = constantRepository.findByConstant("DateFormat");
        locationDto.setCreatedTime(CommonUtls.datetoString(l.getCreatedTime(),c.getConstantValue()));
        locationDto.setUpdatedTime(CommonUtls.datetoString(l.getUpdatedTime(),c.getConstantValue()));
        return locationDto;
    }

    @SuppressWarnings("unchecked")
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

    public List<String> location(String location) {
        List<Location> locationGet = locationRepository.findAllByLocation(location);
        List<String> dto = locationGet.stream()
                .flatMap(loc -> loc.getLab().stream())
                .map(LabLocation::getLab)
                .collect(Collectors.toList());
        return dto;
    }

    public List<DropDownDTO> department(){
        List<LookupItems> li = lookupItemsRepository.findByLookupCategoryNameOrderByDisplayOrderAsc("Department");
        List<String> loc =  locationRepository.findAllDistinctLocation();
        return li.stream()
                .filter(l -> {
                    String key = l.getKey();
                    return key == null || !loc.contains(key);
                })
                .map(l -> new DropDownDTO(l.getId(), l.getValue()))
                .toList();
    }




}