package com.bharatonjava.therapymanager.utils;

public interface Constants {

	public static final String VIEW_HOMEPAGE = "homepage";
	public static final String VIEW_LOGINPAGE = "user.login.form";
	public static final String VIEW_PATIENT_PROFILE = "patient.profile";
	public static final String VIEW_PATIENT_REGISTER_FORM = "patient.register.form";
	public static final String VIEW_PATIENT_SEARCH_RESULT = "patient.search.result";
	public static final String VIEW_PATIENT_PRESCRIPTION_FORM = "patient.prescription.form";
	public static final String VIEW_PATIENT_ASSESMENT_FORM = "patient.assesment.form";
	public static final String VIEW_PATIENT_TREATMENT_VIEW = "patient.treatment.page";
	public static final String VIEW_PATIENT_HISTORY = "patient.history.page";
	public static final String VIEW_ALL_PATIENTS = "patients.list";
	public static final String VIEW_DAILY_EARNINGS = "report.dailyEarnings";
	// views for settings pages
	public static final String VIEW_SETTINGS_TREATMENTS = "settings.treatments";
	

	public static final Long PAGE_SIZE = 20L;
	
	String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	String DATE_FORMAT = "dd-MM-yyyy";
	String DATE_FORMAT_SLASHED = "dd/MM/yyyy";
	
	String ENUM_GROUP_BLOOD_GROUP  = "BLOOD_GROUP";
	
	String ACTION_NEW = "new";
	String ACTION_EDIT = "edit";
	
}
