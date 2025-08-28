package com.empOnboarding.api.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.PdfDTO;

import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;


import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepositrory;

    private final TaskRepository taskRepository;

    private final AuditTrailService auditTrailService;

    private final ConstantRepository constantRepository;

    private final EmployeeQuestionService employeeQuestionService;

    private final MailerService mailerService;

    private final TaskService taskService;


    public EmployeeService(EmployeeRepository employeeRepositrory, AuditTrailService auditTrailService,
                           ConstantRepository constantRepository,EmployeeQuestionService employeeQuestionService,
                           MailerService mailerService, TaskService taskService,
                           TaskRepository taskRepository) {
        this.employeeRepositrory = employeeRepositrory;
        this.auditTrailService = auditTrailService;
        this.constantRepository = constantRepository;
        this.employeeQuestionService = employeeQuestionService;
        this.mailerService = mailerService;
        this.taskService = taskService;
        this.taskRepository = taskRepository;
    }

    public Boolean createEmployee(EmployeeDTO empDto, CommonDTO dto, UserPrincipal user) {
        List<Employee> eDto = new ArrayList<>();
        Employee emp = new Employee(null, empDto.getEmail(), empDto.getName(), empDto.getDepartment(), empDto.getRole(), empDto.getLevel(),
                empDto.getTotalExperience(), empDto.getPastOrganization(), empDto.getLabAllocation(), empDto.getComplianceDay(),
                LocalDate.parse(empDto.getDate()), new Date(), new Date(), new Users(user.getId()), new Users(user.getId()));
        employeeRepositrory.save(emp);
        eDto.add(emp);
        taskService.createTask(eDto, user);
        employeeQuestionService.createEmployeeQuestion(emp.getLevel(),emp.getId());
        dto.setSystemRemarks(emp.toString());
        dto.setModuleId(emp.getName());
        auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
        return true;
    }


//    public void createTask(List<Employee> eDto, CommonDTO cDto){
//        for(Employee dto: eDto){
//            List<QuestionLevel> qL=  questionLevelRepository.findAllByLevel(dto.getLevel());
//            List<Questions> q = questionRepository.findAllById(qL.stream().map(m -> m.getQuestionId().getId()).collect(Collectors.toList()));
//            Map<Long,List<Questions>> groupByGroupId = q.stream().collect(Collectors.groupingBy(m -> m.getGroupId().getId()));
//            groupByGroupId.forEach((groupId, questionsList) -> {
//                Task task = new Task();
//                Users assignedTo = new Users();
//                task.setId(nextId());
//                task.setEmployeeId(dto);
//                task.setCreatedBy(new Users(cDto.getLoginUserId()));
//                task.setUpdatedBy(new Users(cDto.getLoginUserId()));
//                task.setCreatedTime(new Date());
//                task.setUpdatedTime(new Date());
//                for(Questions qn:questionsList){
//                    TaskQuestions tq = new TaskQuestions();
//                    assignedTo = qn.getGroupId().getPgLead();
//                    tq.setQuestionId(qn);
//                    tq.setTaskId(task);
//                    task.getTaskQuestions().add(tq);
//                }
//                task.setAssignedTo(assignedTo);
//                taskRepository.save(task);
//            });
//        }
//    }


    public EmployeeDTO populateEmployee(Employee emp) {
        EmployeeDTO eDto = new EmployeeDTO();
        eDto.setId(emp.getId().toString());
        eDto.setName(emp.getName());
        eDto.setEmail(emp.getEmail());
        eDto.setDepartment(emp.getDepartment());
        eDto.setRole(emp.getRole());
        eDto.setLevel(emp.getLevel());
        eDto.setTotalExperience(emp.getTotalExperience());
        eDto.setPastOrganization(emp.getPastOrganization());
        eDto.setLabAllocation(emp.getLabAllocation());
        eDto.setComplianceDay(emp.getComplainceDay());
        Constant c = constantRepository.findByConstant("DateFormat");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(c.getConstantValue());
        String date = emp.getDate().format(formatter);
        eDto.setDate(date);
        eDto.setCreatedTime(CommonUtls.datetoString(emp.getCreatedTime(), c.getConstantValue()));
        eDto.setUpdatedTime(CommonUtls.datetoString(emp.getUpdatedTime(), c.getConstantValue()));
        eDto.setDeleteFlag(!taskRepository.existsByEmployeeId(emp));
        return eDto;
    }

    public EmployeeDTO findById(Long id) {
        EmployeeDTO eDTO = null;
        Optional<Employee> isEmployee = employeeRepositrory.findById(id);
        if (isEmployee.isPresent()) {
            eDTO = populateEmployee(isEmployee.get());
        }
        return eDTO;
    }

    public Boolean updateEmployee(EmployeeDTO eDto, CommonDTO dto, UserPrincipal userp) {
        Optional<Employee> eOpt = employeeRepositrory.findById(Long.valueOf(eDto.getId()));
        boolean result = false;
        if (eOpt.isEmpty()) {
            mailerService.sendEmailOnException(null);
        } else {
            Employee e = getEmployee(eDto, userp, eOpt);
            employeeRepositrory.save(e);
            dto.setSystemRemarks(e.toString());
            dto.setModuleId(e.getName());
            auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
            result = true;
        }
        return result;
    }

    private static Employee getEmployee(EmployeeDTO eDto, UserPrincipal userp, Optional<Employee> eOpt) {
        Employee e = eOpt.get();
        e.setName(eDto.getName());
        e.setDate(LocalDate.parse(eDto.getDate()));
        e.setDepartment(eDto.getDepartment());
        e.setRole(eDto.getRole());
        e.setLevel(eDto.getLevel());
        e.setTotalExperience(eDto.getTotalExperience());
        e.setPastOrganization(eDto.getPastOrganization());
        e.setLabAllocation(eDto.getLabAllocation());
        e.setComplainceDay(eDto.getComplianceDay());
        e.setUpdatedTime(new Date());
        e.setUpdatedBy(new Users(userp.getId()));
        return e;
    }

    public void deleteEmployee(Long id, CommonDTO dto) {
        try {
            employeeRepositrory.deleteById(id);
            dto.setModuleId("NA");
            dto.setSystemRemarks(Constants.GROUP_DELETE.getValue());
            auditTrailService.saveAuditTrail(Constants.DATA_DELETE.getValue(), dto);
        } catch (Exception e) {
            mailerService.sendEmailOnException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public JSONObject filteredEmployees(String pageNo, String search) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<EmployeeDTO> list;
        Page<Employee> empList;
        if (!CommonUtls.isCompletlyEmpty(search)) {
            empList = employeeRepositrory.findAllBySearch(search, pageable);
        } else {
            empList = employeeRepositrory.findAllByOrderByCreatedTimeDesc(pageable);
        }
        list = empList.stream().map(this::populateEmployee).collect(Collectors.toList());
        json.put("commonListDto", list);
        json.put("totalElements", empList.getTotalElements());
        return json;
    }

    public PdfDTO generateExcel(CommonDTO dto) throws Exception {
        final String SHEET_NAME = "Add Employee Sample Download";
        final String documentFileName = SHEET_NAME + ".xls";

        // Header order drives column positions
        final List<String> headers = Arrays.asList(
                "Candidate Name", "Email", "DOJ", "Department", "Role", "Level",
                "Total Experience", "Past Organization", "Lab Allocation", "Compliance Day"
        );

        // Dropdown lists (can be extended later)
        final String[] LEVEL_LIST = {"L1", "L2"};
        final String[] LAB_LIST = {"Lab1", "Lab2"};

        // Rows to which validations apply (row 0 = header)
        final int FIRST_DATA_ROW = 1;   // second row visually
        final int LAST_DATA_ROW = 1000;

        try (HSSFWorkbook workbook = new HSSFWorkbook();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            HSSFSheet sheet = workbook.createSheet(SHEET_NAME);

            // Styles
            HSSFCellStyle headerStyle = createCellStyle(workbook); // you already have this
            HSSFCellStyle dateStyle = createDateCellStyle(workbook);

            // Header
            createHeaderRow(sheet, headers, headerStyle);

            // Column indexes (resolved from headers)
            final int dojColIdx = headers.indexOf("DOJ");
            final int levelColIdx = headers.indexOf("Level");
            final int labColIdx = headers.indexOf("Lab Allocation");

            // Pre-create date cells with date style for user convenience (optional but nice)
            if (dojColIdx >= 0) {
                for (int r = FIRST_DATA_ROW; r <= LAST_DATA_ROW; r++) {
                    Row row = sheet.getRow(r) != null ? sheet.getRow(r) : sheet.createRow(r);
                    Cell c = row.createCell(dojColIdx);
                    c.setCellStyle(dateStyle);
                }
            }

            // Dropdowns
            if (levelColIdx >= 0) {
                addExplicitListValidation(sheet, FIRST_DATA_ROW, LAST_DATA_ROW, levelColIdx, LEVEL_LIST);
            }
            if (labColIdx >= 0) {
                addExplicitListValidation(sheet, FIRST_DATA_ROW, LAST_DATA_ROW, labColIdx, LAB_LIST);
            }

            // Freeze header and autosize for readability
            sheet.createFreezePane(0, 1);
            for (int c = 0; c < headers.size(); c++) sheet.autoSizeColumn(c);

            // Serialize workbook
            workbook.write(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            PdfDTO excel = new PdfDTO(byteArray, documentFileName);

            // Audit trail (unchanged)
            dto.setModuleId(Constants.ADD_EMPLOYEE);
            dto.setModule(Constants.ADD_EMPLOYEE);
            dto.setSystemRemarks("Add Employee Excel has been downloaded");
            auditTrailService.saveAuditTrail(Constants.EXCEL_EXPORT.getValue(), dto);

            return excel;
        }
    }

    private HSSFCellStyle createDateCellStyle(HSSFWorkbook workbook) {
        HSSFDataFormat df = workbook.createDataFormat();
        HSSFCellStyle style = workbook.createCellStyle();
        style.setDataFormat(df.getFormat("dd-mmm-yyyy")); // e.g., 26-Aug-2025
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * Add an explicit list dropdown to a single column across a row range (.xls / HSSF).
     */
    private void addExplicitListValidation(HSSFSheet sheet,
                                           int firstRow, int lastRow,
                                           int columnIndex,
                                           String[] values) {
        HSSFDataValidationHelper helper = new HSSFDataValidationHelper(sheet);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(values);
        CellRangeAddressList region = new CellRangeAddressList(firstRow, lastRow, columnIndex, columnIndex);
        HSSFDataValidation validation = (HSSFDataValidation) helper.createValidation(constraint, region);

        // Better UX + stricter enforcement
        validation.setSuppressDropDownArrow(false);
        validation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validation.createErrorBox("Invalid choice", "Select a value from the dropdown list.");
        validation.createPromptBox("Select from list", "Choose one of the predefined options.");

        sheet.addValidationData(validation);
    }

    private void createHeaderRow(HSSFSheet sheet, List<String> headers, HSSFCellStyle cellStyle) {
        HSSFRow row = sheet.createRow(0);
        for (int i = 0; i < headers.size(); i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(cellStyle);
        }
    }

    private HSSFCellStyle createCellStyle(HSSFWorkbook workbook) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    private static final String COL_NAME = "name";
    private static final String COL_EMAIL = "email";
    private static final String COL_DEPARTMENT = "department";
    private static final String COL_ROLE = "role";
    private static final String COL_LEVEL = "level";
    private static final String COL_TOTAL_EXPERIENCE = "total_experience";
    private static final String COL_PAST_ORGANIZATION = "past_organization";
    private static final String COL_LAB_ALLOCATION = "lab_allocation";
    private static final String COL_COMPLIANCE_DAY = "compliance_day";
    private static final String COL_DATE_OF_JOINING = "date_of_joining";

    private static final Map<String, String> HEADER_ALIASES = Map.ofEntries(
            Map.entry("candidate name", COL_NAME),
            Map.entry("name", COL_NAME),
            Map.entry("email", COL_EMAIL),
            Map.entry("doj", COL_DATE_OF_JOINING),
            Map.entry("date_of_joining", COL_DATE_OF_JOINING),
            Map.entry("department", COL_DEPARTMENT),
            Map.entry("role", COL_ROLE),
            Map.entry("level", COL_LEVEL),
            Map.entry("total experience", COL_TOTAL_EXPERIENCE),
            Map.entry("total_experience", COL_TOTAL_EXPERIENCE),
            Map.entry("past organization", COL_PAST_ORGANIZATION),
            Map.entry("past_organization", COL_PAST_ORGANIZATION),
            Map.entry("lab allocation", COL_LAB_ALLOCATION),
            Map.entry("lab_allocation", COL_LAB_ALLOCATION),
            Map.entry("compliance day", COL_COMPLIANCE_DAY),
            Map.entry("compliance_day", COL_COMPLIANCE_DAY)

    );

    private static final List<DateTimeFormatter> DATE_PATTERNS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    @SuppressWarnings("unchecked")
    @Transactional
    public JSONObject readExcelFile(Workbook workbook, CommonDTO commonDto, UserPrincipal user) {
        JSONObject result = new JSONObject();
        JSONArray errors = new JSONArray();
        List<Employee> toPersist = new ArrayList<>();
        int success = 0;

        try (workbook) {
            Sheet sheet = Optional.ofNullable(workbook.getSheetAt(0))
                    .orElseThrow(() -> new IllegalArgumentException("Excel has no sheets"));
            if (sheet.getPhysicalNumberOfRows() < 2) {
                result.put("successCount", 0);
                result.put("errorCount", 0);
                result.put("errors", errors);
                return result;
            }
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colIndex = buildHeaderIndex(headerRow);
            List<String> required = List.of(COL_NAME);
            for (String req : required) {
                if (!colIndex.containsKey(req)) {
                    errors.put(errorJson(1, "Missing required column: " + req));
                }
            }
            if (!errors.isEmpty()) {
                result.put("successCount", 0);
                result.put("errorCount", errors.length());
                result.put("errors", errors);
                return result;
            }
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                try {
                    String name = getCellString(row, colIndex.get(COL_NAME));
                    String email = getCellString(row, colIndex.get(COL_EMAIL));
                    String department = getCellString(row, colIndex.get(COL_DEPARTMENT));
                    String role = getCellString(row, colIndex.get(COL_ROLE));
                    String level = getCellString(row, colIndex.get(COL_LEVEL));
                    String totalExperience = getCellString(row, colIndex.get(COL_TOTAL_EXPERIENCE));
                    String pastOrganization = getCellString(row, colIndex.get(COL_PAST_ORGANIZATION));
                    String labAllocation = getCellString(row, colIndex.get(COL_LAB_ALLOCATION));
                    Integer complianceDay = getCellInteger(row, colIndex.get(COL_COMPLIANCE_DAY));
                    LocalDate dateOfJoining = getCellLocalDate(row, colIndex.get(COL_DATE_OF_JOINING));

                    // Basic validations
                    if (name == null || name.isBlank()) {
                        throw new IllegalArgumentException("Name is required");
                    }
                    if (complianceDay != null && complianceDay < 0) {
                        throw new IllegalArgumentException("Compliance day must be >= 0");
                    }

                    // Build entity
                    Employee emp = new Employee();
                    emp.setName(name);
                    emp.setEmail(email);
                    emp.setDepartment(nullIfBlank(department));
                    emp.setRole(nullIfBlank(role));
                    emp.setLevel(nullIfBlank(level));
                    emp.setTotalExperience(nullIfBlank(totalExperience));
                    emp.setPastOrganization(nullIfBlank(pastOrganization));
                    emp.setLabAllocation(nullIfBlank(labAllocation));
                    if (complianceDay != null) emp.setComplainceDay(String.valueOf(complianceDay));
                    emp.setDate(dateOfJoining);
                    emp.setCreatedBy(new Users(commonDto.getLoginUserId()));
                    emp.setUpdatedBy(new Users(commonDto.getLoginUserId()));
                    emp.setCreatedTime(new Date());
                    emp.setUpdatedTime(new Date());
                    toPersist.add(emp);
                    success++;

                } catch (Exception rowEx) {
                    errors.put(errorJson(r + 1, rowEx.getMessage())); // +1 for human row number
                }
            }
            if (!toPersist.isEmpty()) {
                employeeRepositrory.saveAll(toPersist);
                taskService.createTask(toPersist, user);
                toPersist.stream()
                        .filter(e -> e.getId() != null && !CommonUtls.isCompletlyEmpty(e.getLevel()))
                        .forEach(e -> employeeQuestionService.createEmployeeQuestion(e.getLevel().trim(), e.getId()));
            }

            result.put("successCount", success);
            result.put("errorCount", errors.length());
            result.put("errors", errors);
            return result;

        } catch (Exception e) {
            JSONObject fatal = new JSONObject();
            fatal.put("row", 0);
            fatal.put("message", "Failed to read Excel: " + e.getMessage());
            errors.put(fatal);

            result.put("successCount", 0);
            result.put("errorCount", errors.length());
            result.put("errors", errors);
            return result;
        }
    }

    private Map<String, Integer> buildHeaderIndex(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;

        short minColIx = headerRow.getFirstCellNum();
        short maxColIx = headerRow.getLastCellNum();

        for (int c = minColIx; c < maxColIx; c++) {
            Cell cell = headerRow.getCell(c);
            if (cell == null) continue;
            String raw = cell.getStringCellValue();
            if (raw == null) continue;
            String key = normalizeHeader(raw);
            String canonical = HEADER_ALIASES.getOrDefault(key, key);
            map.put(canonical, c);
        }
        return map;
    }

    private String normalizeHeader(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    @SuppressWarnings("unchecked")
    private JSONObject errorJson(int rowNumber, String message) {
        JSONObject j = new JSONObject();
        j.put("row", rowNumber);
        j.put("message", message);
        return j;
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String getCellString(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Convert Excel date to ISO string if needed
                    LocalDate ld = cell.getLocalDateTimeCellValue().toLocalDate();
                    return ld.toString();
                }
                // Avoid scientific notation: use long if integer
                double d = cell.getNumericCellValue();
                if (d == Math.rint(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private Integer getCellInteger(Row row, Integer colIdx) {
        String s = getCellString(row, colIdx);
        if (s == null || s.isBlank()) return null;
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            // maybe it's float like "1.0"
            try {
                double d = Double.parseDouble(s.trim());
                return (int) d;
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid integer: " + s);
            }
        }
    }

    private LocalDate getCellLocalDate(Row row, Integer colIdx) {
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate();
        }

        String s = getCellString(row, colIdx);
        if (s == null || s.isBlank()) return null;

        // Try multiple patterns
        for (DateTimeFormatter fmt : DATE_PATTERNS) {
            try {
                return LocalDate.parse(s.trim(), fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        // Last resort: ISO
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: " + s + " (expected yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy, or MM/dd/yyyy)");
        }
    }

    public Boolean labSave(String lab, Long empId) {
        Employee e = employeeRepositrory.getReferenceById(empId);
        e.setLabAllocation(lab);
        e.setUpdatedTime(new Date());
        employeeRepositrory.save(e);
        return true;
    }

}