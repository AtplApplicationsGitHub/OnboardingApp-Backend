package com.empOnboarding.api.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.AuditTrailDTO;
import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.FilterDTO;
import com.empOnboarding.api.dto.MultiSelectDropDownDTO;
import com.empOnboarding.api.dto.UserMultiSelectDropDown;
import com.empOnboarding.api.entity.AuditTrail;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.AuditTrailRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class AuditTrailService {
	final AuditTrailRepository auditTrailDao;

	final MailerService mailerService;

	final UsersRepository userRepo;

	public AuditTrailService(AuditTrailRepository auditTrailDao, MailerService mailerService, UsersRepository userRepo) {
		this.auditTrailDao = auditTrailDao;
		this.mailerService = mailerService;
		this.userRepo = userRepo;
	}

	public List<AuditTrailDTO> loadAuditTrail(Long userId) {
		List<AuditTrailDTO> list;
		List<AuditTrail> auditList = auditTrailDao.findByUsersIdOrderByIdDesc(userId);
		list = populateAuditTrailDto(auditList);
		return list;
	}

	public List<AuditTrailDTO> loadUserAuditTrailBasedOnDateRange(Long userId, String fDate, String tdate)
			throws Exception {
		List<AuditTrailDTO> list = new ArrayList<>();
		if (!CommonUtls.isCompletlyEmpty(fDate) && !CommonUtls.isCompletlyEmpty(tdate)) {
			Date fromDate = CommonUtls.convertDatePattern(fDate);
			Date toDate = CommonUtls.convertDatePattern(tdate);
			List<AuditTrail> auditList = auditTrailDao.findByUsersIdAndFromDataAndToDate(userId, fromDate, toDate);
			list = populateAuditTrailDto(auditList);
		}
		return list;
	}

	public List<AuditTrailDTO> loadAllAuditTrailBasedOnDateRange(String fDate, String tdate, int pageNo) throws Exception {
		List<AuditTrailDTO> list = new ArrayList<>();
		Pageable pageable = PageRequest.of(pageNo, 10);
		Page<AuditTrail> auditList = Page.empty();
		if (!CommonUtls.isCompletlyEmpty(fDate) && !CommonUtls.isCompletlyEmpty(tdate)) {
			Date fromDate = CommonUtls.convertDatePattern(fDate);
			Date toDate = CommonUtls.convertDatePattern(tdate);
			auditList = auditTrailDao.findByFromDataAndToDate(fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		}
		return list;
	}

	private List<AuditTrailDTO> populateAuditTrailDto(List<AuditTrail> auditList) {
		return auditList.stream()
				.map(m -> new AuditTrailDTO(m.getUsers().getId(),
						m.getUsers().getName(), m.getEvent(), m.getIpAddress(),
						CommonUtls.convertTime(m.getCreatedTime()), m.getBrowser(), m.getSystemRemarks(), m.getModule(),
						m.getModuleId(), m.getUserRemarks()))
				.collect(Collectors.toList());
	}

	private List<AuditTrailDTO> populateAuditTrailDto(Page<AuditTrail> auditList) {
		return auditList.stream()
				.map(m -> new AuditTrailDTO(m.getUsers().getId(),
						m.getUsers().getName(), m.getEvent(), m.getIpAddress(),
						CommonUtls.convertTime(m.getCreatedTime()), m.getBrowser(), m.getSystemRemarks(), m.getModule(),
						m.getModuleId(), m.getUserRemarks()))
				.collect(Collectors.toList());
	}
	
	public void saveAuditTrail(String event, CommonDTO commonDTO) {
		CompletableFuture.runAsync(() -> {
			try {
				if (!commonDTO.getSystemRemarks().equalsIgnoreCase(""))
					if (CommonUtls.isVaildNumber(commonDTO.getLoginUserId())) {
						AuditTrail auditTrail = new AuditTrail(null, new Users(commonDTO.getLoginUserId()), event,
								commonDTO.getIpAddress(), new Date(), commonDTO.getSystemRemarks(),
								commonDTO.getUserRemarks(),
								populateRequestedDeviceDetails(commonDTO.getAgentRequestForAuditTrail()),
								commonDTO.getUniqueDocCodeForAudit(), commonDTO.getModuleType(),
								commonDTO.getDocumentPrimaryKey(), commonDTO.getModule(), commonDTO.getModuleId());
						auditTrailDao.save(auditTrail);
					}
			} catch (Exception e) {
				mailerService.sendEmailOnException(e);
			}
		});

	}

	public String populateRequestedDeviceDetails(String userAgentString) {
		String deviceDetails = "";
		String browserAndVersion = "";
		String osAndDevice = "";
		String device = "";
		if (!CommonUtls.isCompletlyEmpty(userAgentString)) {

			String[] dataSplit = userAgentString.split(",");
			for (String s : dataSplit) {
				String test = s.split(":")[0];
				switch (test) {
				case "browser":
					browserAndVersion = s.split(":")[1];
					break;
				case "os":
					osAndDevice = s.split(":")[1];
					break;
				case "device":
					device = s.split(":")[1];
					break;
				default:
					break;
				}
			}
			deviceDetails = browserAndVersion + "," + osAndDevice + " - " + device;
			System.out.println(deviceDetails);
		}
		return deviceDetails;
	}

	public List<AuditTrailDTO> loadAllAuditTrail() {
		List<AuditTrailDTO> list;
		List<AuditTrail> auditList = auditTrailDao.findAll();
		list = populateAuditTrailDto(auditList);
		return list;
	}

	public List<AuditTrailDTO> loadAllAuditTrailBasedOnEvent(String event) {
		List<AuditTrailDTO> list = new ArrayList<>();
		if (!CommonUtls.isCompletlyEmpty(event)) {
			List<AuditTrail> auditList = auditTrailDao.findByEvent(event);
			list = populateAuditTrailDto(auditList);
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public JSONObject findFilteredData(FilterDTO filterDto, int pageNo) throws Exception {
		List<AuditTrailDTO> list = new ArrayList<>();
		Pageable pageable = PageRequest.of(pageNo, 10);
		JSONObject json = new JSONObject();
		Page<AuditTrail> auditList = Page.empty();
		if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate()) && !CommonUtls.isCompletlyEmpty(filterDto.getToDate())
				&& !(filterDto.getSelectedEvent()).isEmpty() && !(filterDto.getSelectedModule()).isEmpty()
				&& !(filterDto.getSelectedUser()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByEvents(filterDto.getSelectedEvent(),
					filterDto.getSelectedModule(), filterDto.getSelectedUser(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedEvent()).isEmpty()
				&& !(filterDto.getSelectedModule()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByEventAndModule(filterDto.getSelectedEvent(),
					filterDto.getSelectedModule(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedModule()).isEmpty()
				&& !(filterDto.getSelectedUser()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByModuleAndUserName(filterDto.getSelectedModule(),
					filterDto.getSelectedUser(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedEvent()).isEmpty()
				&& !(filterDto.getSelectedUser()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByEventAndUserName(filterDto.getSelectedEvent(),
					filterDto.getSelectedUser(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedEvent()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByColumnData(filterDto.getSelectedEvent(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedModule()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByModuleData(filterDto.getSelectedModule(), fromDate,
					toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
				&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate()) && !(filterDto.getSelectedUser()).isEmpty()) {
			Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
			Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
			auditList = auditTrailDao.findByUserName(filterDto.getSelectedUser(), fromDate, toDate,pageable);
			list = populateAuditTrailDto(auditList);
		} else {
			if (!CommonUtls.isCompletlyEmpty(filterDto.getFromDate())
					&& !CommonUtls.isCompletlyEmpty(filterDto.getToDate())) {
				Date fromDate = CommonUtls.convertDatePattern(filterDto.getFromDate());
				Date toDate = CommonUtls.convertDatePattern(filterDto.getToDate());
				auditList = auditTrailDao.findByFromDataAndToDate(fromDate, toDate,pageable);
				list = populateAuditTrailDto(auditList);
			}
		}
		json.put("commonListDto", list);
		json.put("totalElements", auditList.getTotalElements());
		return json;
	}

	public List<MultiSelectDropDownDTO> getEventByName() {
		List<MultiSelectDropDownDTO> list = new ArrayList<>();
		List<String> events = auditTrailDao.loadAllEvents();
		long i = 1;
		for (String event : events) {
			list.add(new MultiSelectDropDownDTO(i++, event));
		}
		return list;
	}

	public List<MultiSelectDropDownDTO> getModuleByName() {
		List<MultiSelectDropDownDTO> list = new ArrayList<>();
		List<String> modules = auditTrailDao.loadAllModules();
		long i = 1;
		for (String module : modules) {
			if (module != null)
				list.add(new MultiSelectDropDownDTO(i++, module));
		}
		return list;
	}

	public List<MultiSelectDropDownDTO> getUserByName() {

		List<MultiSelectDropDownDTO> list = new ArrayList<>();
		List<Users> optional = userRepo.loadAllActiveUsers(Constants.Y, Constants.N);

		if (!optional.isEmpty()) {
			list = optional.stream().map(d -> new MultiSelectDropDownDTO(d.getId(), d.getName()))
					.collect(Collectors.toList());
		}
		return list;
	}

	public List<UserMultiSelectDropDown> getUserDropDown() {
		List<UserMultiSelectDropDown> list = new ArrayList<>();
		List<Users> optional = userRepo.loadAllActiveUsers(Constants.Y, Constants.N);

		if (!optional.isEmpty()) {
			list = optional.stream().map(d -> new UserMultiSelectDropDown(d.getId(), d.getName())).collect(Collectors.toList());
		}
		return list;
	}

	@SuppressWarnings("unchecked")
	public JSONObject findFilteredDataCageAudit(String cage, int pageNo) throws Exception {
		List<AuditTrailDTO> list = new ArrayList<>();
		List<AuditTrailDTO> pageList = new ArrayList<>();
		JSONObject json = new JSONObject();
		int start = pageNo*10;
		List<AuditTrail> auditList;
		int pageSize = 10;
		if(!CommonUtls.isCompletlyEmpty(cage)) {
			auditList = auditTrailDao.findAuditHistoryByCage(cage);
			list = populateAuditTrailDto(auditList);
		}
		for(int i=start; i<(pageNo+1)*pageSize;i++) {
			if(i<list.size()) {
				pageList.add(list.get(i));
			}
		}
		json.put("commonListDto", pageList);
		return json;
	}
	
}
