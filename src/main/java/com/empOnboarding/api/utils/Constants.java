package com.empOnboarding.api.utils;

public enum Constants {
	SUCCESS(true), FAILURE(false), USER_LOGIN("User Login"), USER_LOGOUT("User Logout"), LOGIN_SUCCESS("Login Success"),
	INVALID_USER("Invalid user"), INVALID_PASSWORD("Invalid Password"), EXCEPTION_MAIL_SUBJECT("Employee Onboarding API Exception"),
	ENVIRONMENT("Environment : "), USER_AGENT("Api-User-Agent"), EXCEPTION("\n\nException"), DATA_INSERT("Data Insert"),DATA_UPDATE("Data Update"),
	DATA_DELETE_SUCCESS("has been deleted"),
	DATA_DELETE("Data Delete"),GROUP_DELETE("Group has been deleted"),EMPLOYEE_DELETE("Employee has been deleted"),
	QUESTION_DELETE("Question has been deleted"),EXCEL_EXPORT("Excel Export");

	public static final String Y = "Y";
	public static final String N = "N";
	public static final String AUDIT_DELIMITER = ",";
	public static final String USER_MANAGEMENT = "User Management";
	public static final String GROUPS = "Groups";
	public static final String QUESTIONS = "Questions";
	public static final String EMPLOYEE = "Employee";
	public static final String DEPARTMENT = "Department";
	public static final String LAB = "Lab";
	public static final String ADD_EMPLOYEE = "Add Employee";
	public static final String AUDIT_ADD_DELIMITER = "[added]<#emp#>";
	public static final String AUDIT_REMOVE_DELIMITER = "[removed]<#emp#>";
	public static final String LOOKUP_CATEGORY = "LookUp Category";
	public static final String LOOKUP_ITEMS = "LookUp Items";
	public static final String LAB_ADDED = "The following Labs were added: ";
	public static final String LAB_REMOVED = "The following Labs were removed: ";

//	Mail Subjects

	public static final String WELCOME_MAIL_NOTE_FOR_NEW_EMPLOYEE = "Welcome to Sailife – Your Onboarding Checklist Awaits!";





	private String value;

	Constants(String values) {
		this.value = values;
	}

	public String getValue() {
		return value;
	}

	private boolean isStatus;

	private Constants(boolean isStatus) {
		this.isStatus = isStatus;
	}

	public boolean isStatus() {
		return this.isStatus;

	}
}