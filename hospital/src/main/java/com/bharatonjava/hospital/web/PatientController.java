package com.bharatonjava.hospital.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bharatonjava.hospital.domain.BillableItem;
import com.bharatonjava.hospital.domain.BillingRecord;
import com.bharatonjava.hospital.domain.HospitalEnum;
import com.bharatonjava.hospital.domain.Patient;
import com.bharatonjava.hospital.domain.Prescription;
import com.bharatonjava.hospital.services.BillableItemService;
import com.bharatonjava.hospital.services.EnumService;
import com.bharatonjava.hospital.services.PatientService;
import com.bharatonjava.hospital.utils.Constants;
import com.bharatonjava.hospital.utils.SpringApplicationContext;
import com.bharatonjava.hospital.web.validators.PatientValidator;
import com.bharatonjava.hospital.web.validators.PrescriptionValidator;

@Controller
public class PatientController {

	private static final Logger log = LoggerFactory.getLogger(PatientController.class);
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private PatientValidator patientValidator;
	
	@Autowired
	private PrescriptionValidator prescriptionValidator;
	
	@Autowired
	private EnumService enumService;
	
	@Autowired
	private BillableItemService billableItemService;
	
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}
	
	public void setPatientValidator(PatientValidator patientValidator) {
		this.patientValidator = patientValidator;
	}
	
	public void setPrescriptionValidator(
			PrescriptionValidator prescriptionValidator) {
		this.prescriptionValidator = prescriptionValidator;
	}
	
	public void setEnumService(EnumService enumService) {
		this.enumService = enumService;
	}
	
	public void setBillableItemService(BillableItemService billableItemService) {
		this.billableItemService = billableItemService;
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		BillableItemEditor billableItemEditor = (BillableItemEditor) SpringApplicationContext.getApplicationContext().getBean("billableItemEditor");
	    binder.registerCustomEditor(BillableItem.class, billableItemEditor);
	    
	    // CONVERT empty date to null
	    SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_SLASHED);
	    dateFormat.setLenient(false);
	    // true passed to CustomDateEditor constructor means convert empty String to null
	    binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	    
	}
	
	
	@RequestMapping(value="/patient/add", method = RequestMethod.GET)
	public String patientShowForm(Model model){
		Patient patient = new Patient();
		model.addAttribute("patient", patient);
		List<HospitalEnum> hospEnums = this.enumService.getEnumsByGroup("CITY");
		model.addAttribute("hospEnums", hospEnums);
		return "patientRegistration";
	}
	
	/**
	 * This method will handle patient Add and Edit cases
	 * @param model
	 * @param patient
	 * @param errors
	 * @return
	 */
	@RequestMapping(value="/patient/add", method = RequestMethod.POST)
	public String patientSubmit(@ModelAttribute("patient") Patient patient, BindingResult result, ModelMap model){
		
		log.info("New Patient to be saved: "+patient);
		Long savedPatientId = 0L;
		int updateCount = 0;
		
		patientValidator.validate(patient, result);
		
		if(result.hasErrors())
		{
			log.info("Errors:  {} ",result.getAllErrors());
			List<HospitalEnum> hospEnums = this.enumService.getEnumsByGroup("CITY");
			model.addAttribute("hospEnums", hospEnums);
			return "patientRegistration";
		}
		
		Long patientId = null;
		if(patient.getPersonId() != null)
		{
			log.info("Updating patient");
			patientService.updatePatient(patient);
		}else{
			log.info("Inserting patient");
			patientId = patientService.savePatient(patient);
		}
		
		model.addAttribute("status", "success");
		//model.addAttribute("patientId", patientId);
			
		//return "redirect:/patient/add";
		return "redirect:/patients/" + patientId;
	}
	
	/**
	 * Patient profile edit
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/patient/edit/{id}", method = RequestMethod.GET)
	public String preparePatientEditForm(@PathVariable Long id, Model model){
		
		Patient patient = patientService.getPatientById(id);
		List<HospitalEnum> hospEnums = this.enumService.getEnumsByGroup("CITY");
		model.addAttribute("hospEnums", hospEnums);
		model.addAttribute("patient", patient);
		return Constants.PATIENT_REGISTRATION_PAGE;
	}
	
	
	@RequestMapping(value="/patient/edit/{id}", method = RequestMethod.POST)
	public String processPatientEditForm(@ModelAttribute("patient") Patient patient, @PathVariable Long id, BindingResult result, Model model){
		
		if(patient.getPersonId() != null && patient.getPersonId() > 0L)
		{
			log.info("Updating patient: {}", patient);
			patientService.updatePatient(patient);
		}else{
			log.info("Not updating patient: {}", patient);
		}
		
		//return Constants.PATIENT_REGISTRATION_PAGE;
		model.addAttribute("update", "success");
		return "redirect:/patients/" + id;
	}
	
	/**
	 * Patient List
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/patients", method = RequestMethod.GET)
	public String allPatients(Model model){

		model.addAttribute("patients", patientService.getAllPatients());
		
		return "listPatients";
	}

	/**
	 * Patient Profile 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/patients/{id}", method = RequestMethod.GET)
	public String getPatientById(@PathVariable Long id, Model model){
		Patient patient = patientService.getPatientById(id);
		model.addAttribute("patient", patient);
		return "patientProfile";
	}
	
	/**
	 * handles patient billing
	 */
	@RequestMapping(value="/patient/billing/{id}", method = RequestMethod.GET)
	public String patientBilling(@PathVariable Long id, Model model){
		// to track patient 
		Patient patient = patientService.getPatientById(id);
		model.addAttribute("patient", patient);
	
		// to display dropdowns
		List<BillableItem> billableItems = billableItemService.getBillableItems();
		model.addAttribute("billableItems", billableItems);
		
		BillingForm billingForm = new BillingForm();
		billingForm.setPatientId(patient.getPersonId());
		
		for(long i = 0L; i < 5; i ++)
		{
			BillingRecord br = new BillingRecord();
			br.setRecordId(i);
			billingForm.getBillingRecords().add(br);
		}
		
		model.addAttribute("billingForm", billingForm);
		
		return "patientBilling";
	}
	
	@RequestMapping(value="/patient/billing/{id}", method = RequestMethod.POST)
	public String processPatientBilling(@ModelAttribute("billingForm") BillingForm billingForm, Long id, BindingResult result, Model model){
		
		log.info("PatientId : {}",billingForm.getPatientId());
		Patient patient = patientService.getPatientById(billingForm.getPatientId());
		model.addAttribute("patient", patient);
		
		log.info("billingForm : "+ billingForm);
		
		
		// to display dropdowns
		List<BillableItem> billableItems =  billableItemService.getBillableItems();
		model.addAttribute("billableItems", billableItems);
		model.addAttribute("billingForm", billingForm);
		
		return "patientBilling";
	}
	
	/**
	 * Default exception handler 
	 */
	@ExceptionHandler(Exception.class)
	public String defaultExceptionHandler(Exception ex){
		log.error("Exception occured in PatientController. ", ex);
		return "error";
	}
	
	/**
	 * handles ajax request to add a new row in patient billing form
	 */
	@RequestMapping(value = "/ajax/patientBillingFormRow", method = RequestMethod.GET)
	public String getPatientBillingFormRow(){
		
		return "patientBillRowAjaxRequest";
	}

	@RequestMapping(value="/patients/{id}/prescription", method = RequestMethod.GET)
	public String showPrescriptionForm(@PathVariable Long id, Model model){
	
		Patient patient = patientService.getPatientById(id);
		model.addAttribute("patient", patient);
		model.addAttribute("prescription", new Prescription());
				
		return "patientPrescriptionForm";
		
		
	}
	
	@RequestMapping(value="/patients/{id}/prescription", method = RequestMethod.POST)
	public String processPrescriptionForm(Prescription prescription, @PathVariable Long id, BindingResult result, Model model){
	
		log.info("Saving Prescription for PatientId: {}, prescription {}",id, prescription);
		
		prescriptionValidator.validate(prescription, result);
		
		Patient patient = patientService.getPatientById(id);
		model.addAttribute("patient", patient);
		// if form has errors, return to same form and display errors
		if(result.hasErrors()){
			log.info("Prescription Validation failed: {}", prescription);
			return "patientPrescriptionForm";
		}
		Long prescriptionId = 0L;
		prescriptionId = patientService.savePrescription(prescription, id);
		
		model.addAttribute("save", "success");
		model.addAttribute("prescriptionId",prescriptionId);
		
		return "redirect:/patients/"+ patient.getPersonId() +"/prescriptions";
	}
	
	@RequestMapping(value="/patients/{id}/prescriptions", method = RequestMethod.GET)
	public String showPrescriptionHistory(@PathVariable("id") Long patientId, Model model){
		Patient patient = patientService.getAllPrescriptions(patientId);
		model.addAttribute("patient", patient);
		return "prescriptionHistory";
	}
	
	//patients/1/prescription/200
	@RequestMapping(value="/patients/{id}/prescriptions/{prescriptionId}", method = RequestMethod.GET)
	public String showPrescriptionDetails(@PathVariable("id") Long patientId,@PathVariable("prescriptionId") Long prescriptionId, Model model){
		Patient patient = patientService.getAllPrescriptions(patientId);
		model.addAttribute("patient", patient);
		return "prescriptionDetailsView";
	}
	
	@RequestMapping(value="/patients/search", method = RequestMethod.GET)
	public void patientSearch(Model model){
		
		model.addAttribute("query", "");
		
	}
	
	@RequestMapping(value="patients/search", method = RequestMethod.POST)
	public String patientSearchHandler(@ModelAttribute("query")String query, Model model){
		
		log.info("Search Query: {}", query);
		
		model.addAttribute("query", query);
		model.addAttribute("patients", patientService.searchPatients(query));
				
		return "listPatients";
	}
}
