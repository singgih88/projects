package com.bharatonjava.therapymanager.web.patient;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.bharatonjava.therapymanager.domain.Assesment;
import com.bharatonjava.therapymanager.domain.HospitalEnum;
import com.bharatonjava.therapymanager.domain.Patient;
import com.bharatonjava.therapymanager.domain.Prescription;
import com.bharatonjava.therapymanager.domain.Treatment;
import com.bharatonjava.therapymanager.services.PatientService;
import com.bharatonjava.therapymanager.utils.Constants;

@Controller
@RequestMapping(value = "/patients")
public class PatientController {

	private static final Logger logger = LoggerFactory
			.getLogger(PatientController.class);

	private PatientValidator patientValidator;
	private PatientService patientService;
	private AssesmentValidator assesmentValidator;

	@Autowired
	public void setPatientValidator(PatientValidator patientValidator) {
		this.patientValidator = patientValidator;
	}

	@Autowired
	public void setPatientService(PatientService patientService) {
		this.patientService = patientService;
	}

	@Autowired
	public void setAssesmentValidator(AssesmentValidator assesmentValidator) {
		this.assesmentValidator = assesmentValidator;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {

		// CONVERT empty date to null
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				Constants.DATE_FORMAT_SLASHED);
		dateFormat.setLenient(false);
		// true passed to CustomDateEditor constructor means convert empty
		// String to null
		/*
		 * binder.registerCustomEditor(LocalDate.class, new CustomDateEditor(
		 * dateFormat, true));
		 */
		binder.registerCustomEditor(Date.class, new CustomDateEditor(
				new SimpleDateFormat("dd/MM/yyyy"), true, 10));
	}

	@ExceptionHandler(Exception.class)
	public String defaultExceptionHandler(Exception ex) {
		logger.error("Exception occured in PatientController. ", ex);
		return "error";
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView listPatients(@RequestParam("page") Long pageNumber,
			ModelAndView mav) {
		
		logger.info("Inside listPatients method.");
		
		List<Patient> patients = this.patientService.getPatients(pageNumber);
		mav.addObject("patients", patients);
		mav.setViewName(Constants.VIEW_ALL_PATIENTS);
		return mav;
	}

	@RequestMapping(value = "/register", method = RequestMethod.GET)
	public ModelAndView registerPatient() {

		logger.info("Inside registerPatient method.");
		
		ModelAndView mav = new ModelAndView();
		Patient patient = new Patient();

		List<HospitalEnum> bloodGroups = this.patientService.getBloodGroups();

		logger.info("bloodGroups : {} ", bloodGroups);
		mav.addObject("patient", patient);
		mav.addObject("bloodGroups", bloodGroups);

		mav.setViewName(Constants.VIEW_PATIENT_REGISTER_FORM);

		return mav;
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public ModelAndView registerPatientHandler(
			@ModelAttribute("patient") Patient patient, BindingResult result,
			ModelMap model) {

		logger.info("Inside registerPatientHandler method. patient={}", patient);
		ModelAndView mav = new ModelAndView();

		patientValidator.validate(patient, result);

		if (result.hasErrors()) {
			logger.debug("Errors in paient form:  {} ", result.getAllErrors());
			// for blood group drop down
			List<HospitalEnum> bloodGroups = this.patientService
					.getBloodGroups();
			mav.addObject("bloodGroups", bloodGroups);
			mav.addObject("patient", patient);
			mav.setViewName(Constants.VIEW_PATIENT_REGISTER_FORM);
			return mav;
		}

		Long patientId = patientService.registerNewPatient(patient);
		model.addAttribute("insertStatus", "success");
		mav.setViewName("redirect:/patients/" + patientId + "/profile");
		return mav;
	}

	@RequestMapping(value = "/{patientId}/profile", method = RequestMethod.GET)
	public ModelAndView patientProfile(@PathVariable("patientId") Long patientId) {

		logger.info("Inside patientProfile method. patientId={}", patientId);
		
		Patient patient = this.patientService.getPatientById(patientId);

		ModelAndView mav = new ModelAndView();
		mav.addObject("patient", patient);
		mav.setViewName(Constants.VIEW_PATIENT_PROFILE);
		return mav;
	}

	@RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
	public String preparePatientEditForm(@PathVariable("id") Long patientId,
			Model model) {

		logger.info("Inside preparePatientEditForm method. patientId={}", patientId);
		
		Patient patient = patientService.getPatientById(patientId);
		List<HospitalEnum> bloodGroups = this.patientService.getBloodGroups();
		model.addAttribute("bloodGroups", bloodGroups);
		model.addAttribute("patient", patient);
		return Constants.VIEW_PATIENT_REGISTER_FORM;

	}

	@RequestMapping(value = "/{id}/edit", method = RequestMethod.POST)
	public String processPatientEditForm(
			@ModelAttribute("patient") Patient patient,
			@PathVariable("id") Long patientId, BindingResult result,
			ModelMap model) {

		logger.info("Inside processPatientEditForm method. patientId={}, patient=", patientId,patient );

		if (patientId != null && patientId > 0L
				&& patientId.equals(patient.getPatientId())) {
			patientService.updatePatient(patient);
			patient = null;
			patient = this.patientService.getPatientById(patientId);
		} else {
			logger.error(
					"Unexpected value of patientId in URL in patient edit form: {} and patient record was {}",
					patientId, patient);
			List<HospitalEnum> bloodGroups = this.patientService
					.getBloodGroups();
			model.addAttribute("bloodGroups", bloodGroups);
			model.addAttribute("patient", patient);
			return Constants.VIEW_PATIENT_REGISTER_FORM;
		}

		model.addAttribute("patient", patient);
		return Constants.VIEW_PATIENT_PROFILE;

	}

	/**
	 * Displays new prescription form
	 * 
	 * @param id
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/{id}/prescription", method = RequestMethod.GET)
	public String showPrescriptionForm(
			@PathVariable("id") Long id,
			@RequestParam(value = "prescriptionId", defaultValue = "0", required = false) Long prescriptionId,
			@RequestParam(value = "action", defaultValue = Constants.ACTION_NEW, required = false) String action,
			Model model) {

		logger.info("Inside showPrescriptionForm method. "
				+ "patientId={}, prescriptionId={}, action = {}", id,
				prescriptionId, action);

		if (action == null || action.equals(Constants.ACTION_NEW)) {

			Patient patient = patientService.getPatientById(id);
			model.addAttribute("patient", patient);
			model.addAttribute("prescription", new Prescription());

		} else if (action != null && action.equals(Constants.ACTION_EDIT)) {

			Prescription prescription = patientService
					.getPrescription(prescriptionId);
			model.addAttribute("prescription", prescription);
			Patient patient = patientService.getPatientById(prescription
					.getPatientId());
			model.addAttribute("patient", patient);

		}

		return Constants.VIEW_PATIENT_PRESCRIPTION_FORM;
	}

	@RequestMapping(value = "/{id}/assesment", method = RequestMethod.GET)
	public String showAssesmentForm(
			@PathVariable("id") Long patientId,
			@RequestParam(value = "assesmentId", defaultValue = "0", required = false) Long assesmentId,
			@RequestParam(value = "action", defaultValue = Constants.ACTION_NEW, required = false) String action,
			Model model) {

		logger.info(
				"Inside showAssesmentForm method. patientId={}, assesmentId={}, action = {}",
				patientId, assesmentId, action);

		Assesment assesment = new Assesment();
		model.addAttribute("assesment", assesment);

		return Constants.VIEW_PATIENT_ASSESMENT_FORM;
	}

	/**
	 * Handles add/edit processing of patientAssessment form
	 * 
	 * @param assesment
	 * @param patientId
	 * @param assesmentId
	 * @param action
	 * @param model
	 * @param result
	 * @return
	 */
	@RequestMapping(value = "/{id}/assesment", method = RequestMethod.POST)
	public String processAssesmentForm(
			@ModelAttribute("assesment") Assesment assesment,
			@PathVariable("id") Long patientId,
			@RequestParam(value = "assesmentId", defaultValue = "0", required = false) Long assesmentId,
			@RequestParam(value = "action", defaultValue = Constants.ACTION_NEW, required = false) String action,
			Model model, BindingResult result) {

		logger.info(
				"Inside processAssesmentForm method patientId={}, assesment={}, action={}",
				patientId, assesment, action);

		assesmentValidator.validate(assesment, result);

		if (result.hasErrors()) {
			logger.info("Errors in assesment form:  {} ", result.getAllErrors());
			return Constants.VIEW_PATIENT_ASSESMENT_FORM;
		}
		// save assessment record to database and send user to add treatment
		// page.
		assesment.setPatientId(patientId);
		Long persistedAssesmentId = patientService
				.createNewAssesment(assesment);
		logger.info("Assesment saved with assesmentId:{}", persistedAssesmentId);

		return "redirect:/patients/" + patientId + "/treatment";
	}

	@RequestMapping(value = "/{id}/treatment", method = RequestMethod.GET)
	public ModelAndView patientTreatmentForm(
			@PathVariable("id") Long patientId,
			@RequestParam(value = "assessmentId", defaultValue = "0", required = false) Long assessmentId,
			@RequestParam(value = "sittings", defaultValue = "0", required = false) String sittings,
			ModelAndView mav, BindingResult result) {

		// fetch patient
		Patient p = patientService.getPatientById(patientId);
		mav.addObject("patient", p);

		// fetch active assessments
		List<Assesment> assesments = this.patientService
				.getAssessmentsForPatient(patientId, true, false);
		mav.addObject("assesments", assesments);

		// treatments for dropdown
		List<Treatment> treatments = this.patientService.getTreatments();
		mav.addObject("treatments", treatments);
		logger.info("{}", treatments);

		// sittings
		mav.addObject("sittings",
				this.patientService.getSittingsForAssessment(assessmentId));

		mav.setViewName(Constants.VIEW_PATIENT_TREATMENT_VIEW);

		return mav;

	}

	@RequestMapping(value = "/{id}/treatment", method = RequestMethod.POST)
	public ModelAndView processPatientTreatmentForm(
			@PathVariable("id") Long patientId,
			@RequestParam(value = "assessmentId", defaultValue = "0", required = false) Long assessmentId,
			@RequestParam(value = "sittings", defaultValue = "0", required = false) String sittings,
			@RequestParam("treatmentId") Long treatmentId, ModelAndView mav,
			BindingResult result) {

		// fetch patient
		Patient p = patientService.getPatientById(patientId);
		mav.addObject("patient", p);

		// fetch active assessments
		List<Assesment> assesments = this.patientService
				.getAssessmentsForPatient(patientId, true, false);
		mav.addObject("assesments", assesments);

		// treatments for dropdown
		List<Treatment> treatments = this.patientService.getTreatments();
		mav.addObject("treatments", treatments);

		// do some validation
		if (assessmentId == 0L) {
			mav.addObject("assessmentNotSelected",
					"Please select Assessment from left panel.");
			logger.info("Assessment was not selected.");
		} else if (treatmentId == null || treatmentId == 0L) {
			mav.addObject("assessmentNotSelected",
					"Please select sitting from dropdown.");
		} else {
			// Add treatment to assessment
			this.patientService.addNewSittingToAssessment(assessmentId,
					treatmentId);

		}

		// sittings
		mav.addObject("sittings",
				this.patientService.getSittingsForAssessment(assessmentId));

		mav.setViewName(Constants.VIEW_PATIENT_TREATMENT_VIEW);
		return mav;
	}

	/**
	 * Handles assessment history and edit assessment request.
	 * @param patientId
	 * @param assessmentId
	 * @param action
	 * @param mav
	 * @return
	 */
	@RequestMapping(value = "/{id}/assessments", method = RequestMethod.GET)
	//@RequestMapping(value = "/{id}/assesment", method = RequestMethod.GET)
	public ModelAndView viewPatientAssessment(
			@PathVariable("id") Long patientId,
			@RequestParam(value = "assessmentId", required = false) Long assessmentId,
			@RequestParam(value = "action", defaultValue = "", required = false) String action,
			ModelAndView mav) {

		logger.info(
				"Inside viewPatientHistory method : patientId={}, assessmentId={}, action={}",
				patientId, assessmentId, action);

		// request to view details
		if (StringUtils.isBlank(action) || (action.equalsIgnoreCase("details"))) {

			Patient p = this.patientService.getPatientById(patientId);
			mav.addObject("patient", p);

			// fetch assessments in brief to show in left panel
			List<Assesment> assesmentsInBrief = this.patientService
					.getAssessmentsInBreifForPatient(patientId);
			mav.addObject("assesmentsInBrief", assesmentsInBrief);

			if (assessmentId != null && assessmentId > 0L) {
				// fetch details
				Assesment assesment = this.patientService.getAssesment(
						patientId, assessmentId);
				mav.addObject("assesment", assesment);
			}

			mav.setViewName(Constants.VIEW_PATIENT_HISTORY);

		} else if (StringUtils.isNotBlank(action)
				&& action.equalsIgnoreCase("edit")) {

			// edit request, Fetch requested assessmentId and send user to edit
			// form
			Assesment assesment = this.patientService.getAssesment(patientId,
					assessmentId);
			mav.addObject("assesment", assesment);
			mav.setViewName(Constants.VIEW_PATIENT_ASSESMENT_FORM);
		}

		return mav;
	}

}
