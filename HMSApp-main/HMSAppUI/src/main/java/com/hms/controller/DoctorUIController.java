package com.hms.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;


@Controller
public class DoctorUIController {

    private final RestTemplate restTemplate;

   
    private String baseUrl="http://localhost:7220";

    public DoctorUIController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    Doctor docSession=null;

    Patient patSession=null;
   
    String role = "admin";
    
    @ModelAttribute
    public void getDoc(@SessionAttribute(name = "docObj", required = false) Doctor docObj) {
    	if (docObj != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+docObj+"  "+docObj.getDoctorId());
	    	docSession = docObj;
	    	
    	}    	
    }
    
    @ModelAttribute
    public void getPatient(@SessionAttribute(name = "patObj", required = false) Patient patObj) {
    	if (patObj != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+patObj+"  "+patObj.getPatientId());
	    	patSession = patObj;
	    	
    	}    	
    }

    @ModelAttribute
    public void getRole(@SessionAttribute(name = "role", required = false) String userRole) {
    	if (userRole != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+role);
	    	role = userRole;
	    	
    	}    	
    }

    
    @GetMapping("/doctorHome")
    public String homePage() {
        return "home"; // This should map to a 'home.html' in the templates folder
    }

    // Add Doctor Form
    @GetMapping("/addDoctorForm")
    public String addDoctorForm(Model model) {
        model.addAttribute("doctor", new Doctor());
        return "addDoctor";
    }

    // Add Doctor
    @PostMapping("doctors/addDoctor")
    public String handleAddDoctor(@ModelAttribute("doctor") Doctor doctor, BindingResult result, Model model) {
        // Use RestTemplate to call backend API
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl+"/doctors/addDoctor",
                doctor,
                String.class
            );
            model.addAttribute("message", response.getBody());
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
				
			
        }
        return "addDoctor";
    }





    // View Doctor By ID Form
    @GetMapping("/viewDoctorByIdForm")
    public String viewDoctorByIdForm(Model model) {
        return "viewDoctorByIdForm";
    }

    // View Doctor By ID
    @GetMapping("/viewDoctorById")
    public String viewDoctorById(@RequestParam int doctorId, Model model) {
        String url = baseUrl + "/doctors/getDoctor/" + doctorId;
        try {
            ResponseEntity<Doctor> response = restTemplate.getForEntity(url, Doctor.class);
            Doctor doctor = response.getBody();
            model.addAttribute("doctor", doctor);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handle 404 error specifically
        	Map<String, String> errors=null;;
			try {
				errors = new ObjectMapper().readValue(
				    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {});
			
				// Map backend error message to Model
				model.addAttribute("errorMessage", errors.get("message")); //mapname.get(key) ->value
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        
       
        } catch (Exception e) {
            // Handle other unexpected exceptions
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        }
        return "viewDoctorByIdForm";
    }


    // View All Doctors
    @GetMapping("/viewAllDoctors")
    public String viewAllDoctors(Model model) {
        String url = baseUrl + "/doctors/getAll";
        try {
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(url, Doctor[].class);
            Doctor[] doctors = response.getBody();
            model.addAttribute("doctors", doctors);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch doctors. Please try again.");
        }
        return "viewAllDoctors";
    }

    // Update Doctor Form
    @GetMapping("/getDocIdToUpdate")
    public String getDocIdToUpdate() {

    	return "getDocIdToUpdate";

    }// Update Doctor Form

    @GetMapping("/updateDoctorForm")
    public String updateDoctorForm(@RequestParam int doctorId, Model model) {

        String url = baseUrl + "/doctors/getDoctor/" + doctorId;

        try {

            ResponseEntity<Doctor> response = restTemplate.getForEntity(url, Doctor.class);

            Doctor doctor = response.getBody();

            model.addAttribute("doctor", doctor);

        } catch (HttpClientErrorException | HttpServerErrorException e) {

            e.printStackTrace();

        	Map<String, String> errors;

            try {

                errors = new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<>() {});

                model.addAttribute("errorMessage", errors.get("message"));

            } catch (JsonProcessingException ex) {

                ex.printStackTrace();

            }

            return "getDocIdToUpdate";

        }

        return "updateDoctor";

    }



    // Update Doctor
    @PostMapping("/doctors/updateDoctor")
    public String updateDoctor(@ModelAttribute Doctor doctor, Model model) {
        String url = baseUrl + "/doctors/updateDoctor/" + doctor.getId();
        try {
            restTemplate.put(url, doctor);
            model.addAttribute("message", "Doctor updated successfully!");
        } 
        catch (HttpClientErrorException | HttpServerErrorException e) {

            e.printStackTrace();
        	Map<String, String> errors;
            try {

                errors = new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<>() {});

                model.addAttribute("errorMessage", errors.get("message"));

            } catch (JsonProcessingException ex) {

                ex.printStackTrace();

            }
        }        
        catch (Exception e) {
            model.addAttribute("error", "Failed to update doctor. Please try again.");
        }
        return "updateDoctor";
    }


    // Delete Doctor Form
    @GetMapping("/deleteDoctorForm")
    public String deleteDoctorForm(Model model) {
        return "deleteDoctorForm";
    }
    

    // Delete Doctor
    @PostMapping("/doctors/deleteDoctor")
    public String deleteDoctor(@RequestParam int doctorId, Model model) {
        String url = baseUrl + "/doctors/leaveDoctor/" + doctorId;
        try {
            restTemplate.put(url, null);
            model.addAttribute("successMessage", "Doctor's status updated to 'left'.");
        } 
        catch (HttpClientErrorException | HttpServerErrorException e) {

            e.printStackTrace();

        	Map<String, String> errors;

            try {

                errors = new ObjectMapper().readValue(e.getResponseBodyAsString(), new TypeReference<>() {});

                model.addAttribute("errorMessage", errors.get("message"));

            } catch (JsonProcessingException ex) {

                ex.printStackTrace();

            }
        }
        catch (Exception e) {
            model.addAttribute("error", "Failed to update doctor's status. Please try again.");
        }
        return "deleteDoctorForm";
    }

    // Search Doctor by Specialization Form
    @GetMapping("/searchBySpecializationForm")
    public String searchBySpecializationForm(Model model) {
    	
    	model.addAttribute("role",role);
        return "searchBySpecializationForm";
    }
    
    @GetMapping("/searchBySpecialization")
    public String searchBySpecialization(@RequestParam String specialization, Model model) {
        String url = baseUrl + "/doctors/getBySpecialization/" + specialization;
        try {
            ResponseEntity<Doctor[]> response = restTemplate.getForEntity(url, Doctor[].class);
            Doctor[] doctors = response.getBody();
            model.addAttribute("doctors", doctors);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", "No doctors found for the given specialization.");
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch doctors. Please try again.");
        }
        model.addAttribute("role",role);
        return "searchBySpecializationForm";
    }

    
    @GetMapping("/freeSlotsForm")
    public String freeSlotsForm(Model model) {
    	model.addAttribute("role",role);
        return "freeSlotsForm";
    }

    // Fetch Free Slots
    @GetMapping("/fetchFreeSlots")
    public String fetchFreeSlots(
            @RequestParam int doctorId,
            @RequestParam String date,
            @RequestParam(defaultValue = "09:00") String clinicStartTime,
            @RequestParam(defaultValue = "17:00") String clinicEndTime,
            Model model) {

        String url = baseUrl + "/doctors/free-slots?doctorId=" + doctorId + 
                     "&date=" + date + 
                     "&clinicStartTime=" + clinicStartTime + 
                     "&clinicEndTime=" + clinicEndTime;

        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<String>>() {}
            );

            List<String> freeSlots = response.getBody();
            model.addAttribute("freeSlots", freeSlots);

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            model.addAttribute("error", "Failed to fetch free slots: " + e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", "An unexpected error occurred. Please try again later.");
        }

        model.addAttribute("role",role);
    	return "freeSlotsForm";
    }
    
}
