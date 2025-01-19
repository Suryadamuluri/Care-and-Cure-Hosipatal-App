package com.hms.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;
import com.hms.entities.UserInfo;

@Controller
public class PatientUIController {

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:7220";
    
    
    Doctor docSession=null;

    Patient patSession=null;
    
    UserInfo userSession = null;
   
    String role = "admin";
    
    @ModelAttribute
    public void getDoc(@SessionAttribute(name = "docObj", required = false) Doctor docObj) {
    	System.out.println("Session obj doc "+docObj);
    	if (docObj != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+docObj+"  "+docObj.getDoctorId());
	    	docSession = docObj;
    	}    	
    }
    
    @ModelAttribute
    public void getPatient(@SessionAttribute(name = "patObj", required = false) Patient patObj) {
    	System.out.println("Session obj pat "+patObj);
    	if (patObj != null) {
	    	
	    	patSession = patObj;
	    	System.out.println("SESSSSSIIOOOOOON  "+patSession+"  "+patSession.getPatientId());
    	}    	
    }

    @ModelAttribute
    public void getUser(@SessionAttribute(name = "userObj", required = false) UserInfo userObj) {
    	System.out.println("Session obj user "+userObj);
    	if (userObj != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+userObj+"  "+userObj.getUserName());
	    	userSession = userObj;
    	}    	
    }

    @ModelAttribute
    public void getRole(@SessionAttribute(name = "role", required = false) String userRole) {
    	if (userRole != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+role);
	    	role = userRole;
    	}    	
    }

    

    @GetMapping("/patienthome")
    public String home() {
        return "home";
    }

    @GetMapping("/addPatientForm")
    public String addPatientForm(Model model) {
        model.addAttribute("patient", new Patient());
        return "addPatient";
    }

    @PostMapping("/addPatients")
    public String handleAddPatient(@ModelAttribute("patient") Patient patient, BindingResult result, Model model) {
        try {
            ResponseEntity<Patient> response = restTemplate.postForEntity(
                BASE_URL + "/api/patient/addPatient",
                patient,
                Patient.class
            );

            Patient patientRes = response.getBody();
            model.addAttribute("messagePat", "Patient added successfully with ID " + patientRes.getPatientId()+" Check your mail for username and password and login to proceed further");
            model.addAttribute("userInfo", new UserInfo());
            return "login";
        } catch (HttpClientErrorException e) {
            // Parse and display validation errors from backend
            Map<String, String> errors=null;;
					try {
						errors = new ObjectMapper().readValue(
						    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
					} catch (JsonMappingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (JsonProcessingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
//				
			
				// Map backend errors to BindingResult				
				for(Map.Entry<String, String> entryset : errors.entrySet()) {
					String field = entryset.getKey();
					String errorMsg = entryset.getValue();							
					result.rejectValue(field,"",errorMsg);
				}
				
            model.addAttribute("error", "Failed to add patient. Please correct the errors.");
            return "addPatient";
        }
    }

    @GetMapping("/viewAllPatients")
    public String viewAllPatients(Model model) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(BASE_URL + "/api/patient/viewAllPatients", List.class);
            model.addAttribute("patients", response.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch patients. Please try again later.");
        }
        return "viewAllPatients";
    }

    @GetMapping("/viewPatientByIdForm")
    public String viewPatientByIdForm(Model model) {
    	 model.addAttribute("role",role);
        return "viewPatientByIdForm";
    }

    @GetMapping("/viewPatientById")
    public String viewPatientById(@RequestParam(value="patientId",required = false) Integer patientId, Model model) {
        try {
        	if(role!=null && role.equals("patient") && patientId==null && patSession!=null) {
        		patientId = patSession.getPatientId();
        	}
        	else if(role.equals("patient") && patientId==null && patSession==null) 
            {
            	model.addAttribute("userInfo", new UserInfo());
                return "login";
            }
            ResponseEntity<Patient> response = restTemplate.getForEntity(
                BASE_URL + "/api/patient/viewPatientById/" + patientId,
                Patient.class
            );
        	model.addAttribute("role",role);

            model.addAttribute("patient", response.getBody());
            return "viewPatientById";
            
        } 
        catch (HttpClientErrorException e) {
            Map<String, String> errors = parseBackendErrors(e);
            model.addAttribute("error", errors != null ? errors.get("message") : "Patient not found.");
            return "viewPatientByIdForm";
        }
    }

    @GetMapping("/viewPatientByNameForm")
    public String viewPatientByNameForm(Model model) {
    	 model.addAttribute("role",role);
        return "viewPatientByNameForm";
    }

    @GetMapping("/viewPatientByName")
    public String viewPatientByName(@RequestParam("name") String name, Model model) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(BASE_URL + "/api/patient/viewPatientByName/" + name, List.class);
            model.addAttribute("role",role);
            model.addAttribute("patients", response.getBody());
            return "viewPatientByName";
        } catch (Exception e) {
            model.addAttribute("error", "No patients found with the name: " + name);
            model.addAttribute("role",role);
            return "viewPatientByNameForm";
        }
    }

    @GetMapping("/updatePatientForm")
    public String updatePatientForm(Model model) {
    	model.addAttribute("role",role);
    	
        model.addAttribute("patient", new Patient());
        return "updatePatientForm";
    }

    @GetMapping("/fetchPatientForUpdate")
    public String fetchPatientForUpdate(@RequestParam(value="patientId",required = false) Integer patientId, Model model) {
        try {
        	
        	if(role!=null && role.equals("patient") && patientId==null && patSession!=null) {
        		patientId = patSession.getPatientId();
        	}
        	else if(role.equals("patient") && patientId==null && patSession==null) 
            {
            	model.addAttribute("userInfo", new UserInfo());
                return "login";
            }
        	
            ResponseEntity<Patient> response = restTemplate.getForEntity(
                BASE_URL + "/api/patient/viewPatientById/" + patientId,
                Patient.class
            );
            model.addAttribute("role",role);
        	
            model.addAttribute("patient", response.getBody());
            return "updatePatient";
        } catch (Exception e) {
            model.addAttribute("error", "Patient not found with ID: " + patientId);
            return "updatePatientForm";
        }
    }

    @PostMapping("/updatePatients")
    public String updatePatient(@ModelAttribute("patient") Patient patient, BindingResult result, Model model) {
        try {
        	HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Patient> request = new HttpEntity<>(patient, headers);

            // Ensure the URL is correctly formed with patientId as a path variable
            ResponseEntity<Patient> response = restTemplate.exchange(
                BASE_URL + "/api/patient/updatePatient/" + patient.getPatientId(), // Correct path variable used here
                HttpMethod.PUT,
                request,
                Patient.class
            );

            model.addAttribute("message", "Patient updated successfully: " + response.getBody().getPatientName());
            return "/patient/patientinfo";
        } catch (HttpClientErrorException e) {
            Map<String, String> errors = parseBackendErrors(e);
            if (errors != null) {
                errors.forEach((field, errorMsg) -> result.rejectValue(field, "", errorMsg));
            }
            model.addAttribute("error", "Failed to update patient. Please correct the errors.");
            return "updatePatient";
        }
    }

    @GetMapping("/viewPatientByMedicalHistoryForm")
    public String viewPatientByMedicalHistoryForm() {
        return "viewPatientByMedicalHistoryForm";
    }

    @GetMapping("/viewPatientByMedicalHistory")
    public String viewPatientByMedicalHistory(@RequestParam("history") String medicalHistory, Model model) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(BASE_URL + "/api/patient/viewPatientByMedicalHistory/" + medicalHistory, List.class);
            model.addAttribute("patients", response.getBody());
            return "viewPatientByMedicalHistory";
        } catch (Exception e) {
            model.addAttribute("error", "No patients found with the medical history: " + medicalHistory);
            return "viewPatientByMedicalHistoryForm";
        }
    }

    @GetMapping("/viewPatientsByDoctorAndDateForm")
    public String viewPatientsByDoctorAndDateForm() {
        return "viewPatientsByDoctorAndDateForm";
    }

    @GetMapping("/viewPatientsByDoctorAndDate")
    public String viewPatientsByDoctorAndDate(
            @RequestParam(name = "doctorId") int doctorId,
            @RequestParam(name = "appDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate appDate,
            Model model) {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    BASE_URL + "/api/patient/by-doctor-and-date?doctorId=" + doctorId + "&appDate=" + appDate, 
                    List.class);

            model.addAttribute("patients", response.getBody());
            return "viewPatientsByDoctorAndDate";
        } catch (Exception e) {

            model.addAttribute("error", 
                    "No patients found for doctorId: " + doctorId + " on date: "+appDate);
            return "viewPatientsByDoctorAndDateForm";
        }

          
    }

    @GetMapping("/no-show-patients")
    public String getNoShowPatients(Model model) {
        try {
            ResponseEntity<List<Patient>> response = restTemplate.exchange(
                BASE_URL + "/api/patient/no-show-report",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Patient>>() {}
            );
            model.addAttribute("noShowPatients", response.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch no-show patients report.");
        }
        return "noShowPatients";
    }

    private Map<String, String> parseBackendErrors(HttpClientErrorException e) {
        try {
            return new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
