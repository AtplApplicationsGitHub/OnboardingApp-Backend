package com.empOnboarding.api.service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.LookupCategoryDTO;
import com.empOnboarding.api.entity.LookupCategory;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.LookupCategoryRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.springframework.stereotype.Service;

@Service
public class LookupCategoryService {

	final
	LookupCategoryRepository lookupCategoryRepo;

	final
	AuditTrailService auditTrailService;

	public LookupCategoryService(LookupCategoryRepository lookupCategoryRepo, AuditTrailService auditTrailService) {
		this.lookupCategoryRepo = lookupCategoryRepo;
		this.auditTrailService = auditTrailService;
	}

	public boolean insertCategory(LookupCategoryDTO lookUpcategoryDTO) throws Exception {
		Optional<LookupCategory> categoryName = lookupCategoryRepo.findFirstByName(lookUpcategoryDTO.getName());
		if(categoryName.isPresent()) {
			throw new Exception(lookUpcategoryDTO.getName()+" : Already Exists");
		}
        LookupCategory lookupCategory = new LookupCategory(lookUpcategoryDTO.getName(),
                new Users(lookUpcategoryDTO.getLoginUserId()), new Date(), null);
        lookUpcategoryDTO.setSystemRemarks(lookupCategory.toString());
        lookUpcategoryDTO.setModule(Constants.LOOKUP_CATEGORY);
        lookUpcategoryDTO.setModuleId(lookupCategory.getName());
        auditTrailService
                .saveAuditTrail(CommonUtls.isEmpty(lookupCategory.getId()) ? Constants.DATA_INSERT.getValue()
                        : Constants.DATA_UPDATE.getValue(), lookUpcategoryDTO);
        lookupCategoryRepo.save(lookupCategory);
        return true;
	}

	public List<LookupCategoryDTO> getCategoryList() {
        return lookupCategoryRepo.findAll().stream().map(this::populateLookupCategory)
                .sorted(Comparator.comparing(LookupCategoryDTO::getUpdatedTime).reversed())
                .collect(Collectors.toList());
    }

	private LookupCategoryDTO populateLookupCategory(LookupCategory lookupCategory) {
		return CommonUtls.getModelMapper().map(lookupCategory, LookupCategoryDTO.class);
	}

	public boolean getCategoryByName(String categoryName) {
		boolean result;
        result = lookupCategoryRepo.existsByName(categoryName);
        return result;
	}

}
