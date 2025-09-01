package com.empOnboarding.api.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.LookupItemsDTO;
import com.empOnboarding.api.entity.LookupCategory;
import com.empOnboarding.api.entity.LookupItems;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.LookupCategoryRepository;
import com.empOnboarding.api.repository.LookupItemsRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.apache.commons.lang3.builder.DiffResult;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class LookupItemsService {

	final LookupItemsRepository lookupItemsRepo;

	final LookupCategoryRepository lookupCategoryRepo;

	final MailerService mailerService;

	final AuditTrailService auditTrailService;

	public LookupItemsService(LookupItemsRepository lookupItemsRepo, LookupCategoryRepository lookupCategoryRepo, MailerService mailerService,
			AuditTrailService auditTrailService) {
		this.lookupItemsRepo = lookupItemsRepo;
		this.lookupCategoryRepo = lookupCategoryRepo;
		this.mailerService = mailerService;
		this.auditTrailService = auditTrailService;
	}

	public boolean insertCategoryItem(LookupItemsDTO lookUpItemDTO) {
		Optional<LookupItems> isLookupItems = Optional.empty();
		String oldValue;
		if (!CommonUtls.isCompletlyEmpty(lookUpItemDTO.getId()))
			isLookupItems = lookupItemsRepo.findById(Long.valueOf(lookUpItemDTO.getId()));
		Optional<LookupCategory> categorylist = lookupCategoryRepo
				.findById(Long.valueOf(lookUpItemDTO.getCategoryId()));
		LookupItems lookupItems = new LookupItems(isLookupItems.map(LookupItems::getId).orElse(null),
				categorylist.get(), lookUpItemDTO.getKey().trim(), lookUpItemDTO.getValue().trim(), lookUpItemDTO.getDisplayOrder(),
				new Users(lookUpItemDTO.getLoginUserId()), new Date());
		oldValue = isLookupItems.map(LookupItems::getKey).orElse(null);
		if (CommonUtls.isEmpty(lookUpItemDTO.getId())) {
			lookUpItemDTO.setSystemRemarks(lookupItems.toString());
		} else {
			Optional<LookupItems> byId = lookupItemsRepo.findById(Long.valueOf(lookUpItemDTO.getId()));
			if (byId.isPresent()) {
				DiffResult<?> diff = byId.get().diff(lookupItems);
				if (!CommonUtls.isCompletlyEmpty(CommonUtls.getDiff(diff)))
					lookUpItemDTO.setSystemRemarks(CommonUtls.getDiff(diff));
			}
		}
		lookupItemsRepo.save(lookupItems);
		auditTrailService.saveAuditTrail(CommonUtls.isEmpty(lookUpItemDTO.getId()) ? Constants.DATA_INSERT.getValue()
				: Constants.DATA_UPDATE.getValue(), lookUpItemDTO);
		String a = oldValue;
		return true;
	}

	@SuppressWarnings("unchecked")
	public JSONObject getCategoryItemsById(String categoryId, String pageNo) {
		Pageable pageable;
		JSONObject json = new JSONObject();
		pageable = PageRequest.of(Integer.parseInt(pageNo), 5);
		Page<LookupItems> itemList = lookupItemsRepo
				.findByLookupCategoryIdOrderByDisplayOrderAsc(Long.valueOf(categoryId), pageable);
		List<LookupItemsDTO> itemLists = itemList.stream().map(this::populateLookupItem).collect(Collectors.toList());
		json.put("commonListDto", itemLists);
		json.put("totalElements", itemList.getTotalElements());
		return json;
	}

	private LookupItemsDTO populateLookupItem(LookupItems lookupItems) {
		LookupItemsDTO map = CommonUtls.getModelMapper().map(lookupItems, LookupItemsDTO.class);
		map.setCategoryId(lookupItems.getLookupCategory().getId().toString());
		return map;
	}

	public List<DropDownDTO> getCategoryByCategoryName(String categoryName) {
		List<DropDownDTO> list = new ArrayList<>();
		Optional<LookupCategory> optional = lookupCategoryRepo.findFirstByName(categoryName);
		if (optional.isPresent()) {
			list = optional.get().getLookupItems().stream()
					.map(d -> new DropDownDTO(d.getId(),d.getKey(), d.getValue(),d.getDisplayOrder()))
					.collect(Collectors.toList());
		}
		list.sort(Comparator.comparing(DropDownDTO::getDisplayOrder));
		return list;
	}

	public boolean updatelookUpItem(LookupItemsDTO lookUpItemDTO) {
		boolean result = false;
		Optional<LookupItems> lookupItemlist = lookupItemsRepo.findById(Long.valueOf(lookUpItemDTO.getId()));
		Optional<LookupCategory> categorylist = lookupCategoryRepo
				.findById(Long.valueOf(lookUpItemDTO.getCategoryId()));
		if (lookupItemlist.isPresent()) {
			LookupItems lookupItem = new LookupItems();
			lookupItem.setId(lookupItemlist.get().getId());
			lookupItem.setKey(lookUpItemDTO.getKey());
			lookupItem.setValue(lookUpItemDTO.getValue());
			lookupItem.setDisplayOrder(lookUpItemDTO.getDisplayOrder());
			lookupItem.setUpdatedTime(new Date());
			lookupItem.setUsers(new Users(lookUpItemDTO.getLoginUserId()));
			lookupItem.setLookupCategory(categorylist.get());
			Optional<LookupItems> oldItems = lookupItemsRepo.findById(Long.valueOf(lookUpItemDTO.getId()));
			DiffResult<?> diff = oldItems.get().diff(lookupItem);
			if (!CommonUtls.isCompletlyEmpty(CommonUtls.getDiff(diff)))
				lookUpItemDTO.setSystemRemarks(CommonUtls.getDiff(diff));
			lookupItemsRepo.save(lookupItem);
			auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), lookUpItemDTO);
			result = true;
		}
		return result;
	}

}