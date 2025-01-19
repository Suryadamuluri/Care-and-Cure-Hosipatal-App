package com.hms.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.entities.Bill;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;
import com.hms.entities.UserInfo;


@Controller
public class BillUIController{

    @Autowired
    private RestTemplate restTemplate;

    private final String BASE_URL = "http://localhost:7220"; // Base URL for the backend API

    
    Patient patSession=null;
    //    Patient patSession;

    String role = "admin";
    
    
    @ModelAttribute
    public void getPatient(@SessionAttribute(name = "patObj", required = false) Patient patObj) {
    	if (patObj != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+patObj+"  "+patObj.getPatientId());
	    	patSession = patObj;
	    	//role="patient";
    	}    	
    }

    @ModelAttribute
    public void getRole(@SessionAttribute(name = "role", required = false) String userRole) {
    	if (userRole != null) {
	    	System.out.println("SESSSSSIIOOOOOON  "+role);
	    	role = userRole;
	    	
    	}    	
    }

    @GetMapping("/billHome") // Renders the home page
    public String home() {
        return "dashboard"; // This will render home.html
    }

    @GetMapping("/addBillForm") // Renders the add bill page
    public String showAddBillPage(Model model) {
        model.addAttribute("bill", new Bill());
      
        return "billAdd"; // This will render billAdd.html
    }
   /* @GetMapping("/patient/{patientId}/pending")
    public String getPendingBills(@PathVariable(value = "patientId", required = false) Integer patientId, Model model) {
       if(patientId==null) {
    	   patientId=patSession.getPatientId();
       }
       
          String url = "http://localhost:7220/api/bills/patient/" + patientId + "/pending"; // Replace with actual backend URL
           Bill[] billsArray = restTemplate.getForObject(url, Bill[].class);
           List<Bill>  pendingBills   = Arrays.asList(billsArray);
        model.addAttribute("pendingBills", pendingBills);
        return "Patient";
    }*/
    
    @GetMapping("/patient/pending")
    public String getPendingBills(Model model) {
        // Use `patientId` from session if available
       // Integer patientId = (patSession != null) ? patSession.getPatientId() : null;
    	Integer patientId=0;
    	if(role!=null && role.equals("patient") && patientId==null && patSession!=null) {
    		patientId = patSession.getPatientId();
    	}
    	else if(role.equals("patient") && patientId==null && patSession==null) 
        {
        	model.addAttribute("userInfo", new UserInfo());
            return "login";
        }
    	
        System.out.println("jwalkdjwkajdcriu hiu hrfwi e.k");
        System.out.println(patientId);

        if (patientId == null) {
            // Handle case when patientId is not available
            model.addAttribute("error", "Patient ID is not available. Please log in.");
            return "redirect:/loginPage"; // Redirect to login page
        }

        // Fetch pending bills from the backend
        String url = "http://localhost:7220/api/bills/patient/" + patientId + "/pending";
        System.out.println(url);
        Bill[] billsArray = restTemplate.getForObject(url, Bill[].class);
        List<Bill> pendingBills = Arrays.asList(billsArray);

        // Add bills to the model
        model.addAttribute("patientId", patientId);
        model.addAttribute("pendingBills", pendingBills);
        return "Patient"; // Return patient page view
    }


    @PostMapping("/addBill/{appointmentId}")
    public String addBill(@PathVariable("appointmentId") Long appointmentId,
                          @ModelAttribute Bill bill, BindingResult result,
                          Model model) {

        // Log the incoming bill object
        System.out.println("Received bill object: " + bill);
        System.out.println("Appointment ID: " + appointmentId);

        // Prepare the URL for the API call
        String url = BASE_URL + "/api/bills/generateBill/" + appointmentId;

        // Remove nested objects that might cause issues
        bill.setAppointment(null);

        try {
            ResponseEntity<Bill> response = restTemplate.postForEntity(url, bill, Bill.class);

            if (response.getStatusCode().is2xxSuccessful()) {
            	 Bill createdBill = response.getBody();

                 // Add the created bill object to the model to pass it to the summary page
                 model.addAttribute("bill", createdBill);

                 // Redirect to the bill summary page
                 return "billSummary";
              
            } else {
                model.addAttribute("errorMessage", "Error occurred while adding the bill. Status code: " + response.getStatusCodeValue());
            }
        } catch (HttpClientErrorException e) {
            try {
                // Parse the backend response as a flat map
                Map<String, String> errorResponse = new ObjectMapper().readValue(
                    e.getResponseBodyAsString(), new TypeReference<Map<String, String>>() {}
                );

                if (errorResponse.containsKey("message")) {
                    // Handle global error
                    String globalMessage = errorResponse.get("message");
                    model.addAttribute("errorMessage", globalMessage);
                } else {
                    // Handle field-specific errors
                    for (Map.Entry<String, String> entry : errorResponse.entrySet()) {
                        String field = entry.getKey();      // Field name
                        String errorMessage = entry.getValue(); // Error message

                        // Reject the value for field-specific errors
                        result.rejectValue(field, "", errorMessage);
                    }
                }
            } catch (JsonProcessingException e1) {
                e1.printStackTrace();
                model.addAttribute("errorMessage", "An unknown error occurred.");
            }
        }

        // Return to the same view with success or error messages
        return "billAdd";
    }


    @GetMapping("/viewBills")
    public String viewBills(@RequestParam(required = false) Integer billId,
                            @RequestParam(required = false) LocalDate startDate,
                            @RequestParam(required = false) LocalDate endDate,
                            Model model) {

        String url = BASE_URL + "/api/bills/viewAllBills";

        // Construct URL based on the filters provided
        List<String> queryParams = new ArrayList<>();
        if (billId != null) {
            queryParams.add("billId=" + billId);
        }
        if (startDate != null && endDate != null) {
            queryParams.add("startDate=" + startDate);
            queryParams.add("endDate=" + endDate);
        }

        if (!queryParams.isEmpty()) {
            url += "?" + String.join("&", queryParams); // Join multiple parameters with "&"
        }

        try {
            // Fetch bills based on filters
            ResponseEntity<List<Bill>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Bill>>() {}
            );

            List<Bill> bills = response.getBody();
            if (bills != null && !bills.isEmpty()) {
                model.addAttribute("bills", bills);
            } else {
                model.addAttribute("errorMessage", "No bills found with the given criteria.");
            }
        } catch (HttpClientErrorException e) {
            // Handle client-side HTTP errors (e.g., 404, 400, etc.)
            String errorMessage = parseErrorMessage(e);
            if (errorMessage == null) {
                // Fallback error message if parsing fails
                errorMessage = "Error fetching bills: " + e.getMessage();
            }

          
            model.addAttribute("errorMessage", errorMessage);
        } catch (Exception e) {
            // Handle unexpected errors (e.g., network issues)
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }

        return "viewAllBill"; // This will render the "viewAllBill.html" template
    }
    @GetMapping("/paid")
    public String showPaidBillsPage(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            Model model) {

        // Check if future date is selected
        if (startDate != null && startDate.isAfter(LocalDate.now()) || endDate != null && endDate.isAfter(LocalDate.now())) {
            model.addAttribute("error", "Date can't be in the future");
            return "paidBills";
        }

        // Construct URL for backend request
        StringBuilder urlBuilder = new StringBuilder("http://localhost:7220/api/bills/paid");

        // Append query parameters only if present
        if (startDate != null && endDate != null) {
            urlBuilder.append("?startDate=").append(startDate).append("&endDate=").append(endDate);
        }

        // Call backend and fetch bills
        Bill[] billsArray = restTemplate.getForObject(urlBuilder.toString(), Bill[].class);

        // Handle response
        List<Bill> paidBills = (billsArray != null) ? Arrays.asList(billsArray) : new ArrayList<>();
        model.addAttribute("paidBills", paidBills);

        // Add date range to model
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        // If no bills found, display a message
        if (paidBills.isEmpty()) {
            model.addAttribute("error", "No bills found for the given date range.");
        }

        return "paidBills"; // Template in resources/templates/paidBills.html
    }



    @GetMapping("/searchBill")
    public String searchBill(@RequestParam("billId") Long billId, Model model) {
        String url = BASE_URL + "/api/bills/viewBill/" + billId; // API call to fetch bill by ID
        ResponseEntity<Bill> response = restTemplate.exchange(url, HttpMethod.GET, null, Bill.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            Bill bill = response.getBody(); // Get the bill object from the response
            model.addAttribute("bill", bill);

            // Log the isInsuranceApplicable value
          //  System.out.println("Received isInsuranceApplicable: " + bill.getIsInsuranceApplicable()); // Log the value

            model.addAttribute("successMessage", "Bill found with ID: " + billId);
        } else {
            model.addAttribute("errorMessage", "Bill not found with ID: " + billId);
            model.addAttribute("bills", List.of()); // Clear any existing bills in case of no results
        }

        return "billView"; // Only update the table container dynamically
    }
   
    @GetMapping("/fetchConsultationFee")
    public String fetchConsultationFee(@RequestParam("appointmentId") int appointmentId, Model model) {
        String url = BASE_URL + "/api/bills/consultationFees/" + appointmentId;

        try {
            // Sending GET request to fetch consultation fee
            ResponseEntity<Double> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                Double.class
            );

            // Validate response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Bill bill = new Bill();
                bill.setConsultationFees(response.getBody());
                model.addAttribute("bill", bill);
            } else {
                model.addAttribute("bill", new Bill()); // Add default Bill object
                model.addAttribute("errorMessage", "Consultation fee not found for Appointment ID: " + appointmentId);
            }
        } catch (HttpClientErrorException e) {
            model.addAttribute("bill", new Bill()); // Add default Bill object in case of error
            String errorMessage = parseErrorMessage(e);
            model.addAttribute("errorMessage", errorMessage != null ? errorMessage : "Error: Unable to fetch consultation fee.");
        } catch (Exception e) {
            model.addAttribute("bill", new Bill()); // Add default Bill object for unexpected errors
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }

        model.addAttribute("appointmentId", appointmentId); // Always add appointmentId
        return "billAdd"; // Render the view
    }

    // Utility method to parse error messages
    private String parseErrorMessage(HttpClientErrorException e) {
        try {
            Map<String, String> errors = new ObjectMapper().readValue(
                e.getResponseBodyAsString(),
                new TypeReference<Map<String, String>>() {}
            );
            return errors.get("message");
        } catch (JsonProcessingException ex) {
            return "Error parsing error response: " + ex.getMessage();
        }
    }
    
    @GetMapping("/updateBill")
    public String showUpdateBillForm(@RequestParam("billId") Integer billId, Model model) {
        // Fetch the bill details from the backend API
        String url = BASE_URL + "/api/bills/viewBill/" + billId;
        try {
            ResponseEntity<Bill> response = restTemplate.exchange(url, HttpMethod.GET, null, Bill.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Bill bill = response.getBody();
                model.addAttribute("bill", bill); // Pass the bill to the form for editing
                return "updateBillForm"; // This is the template where the user will edit the bill
            } else {
                model.addAttribute("errorMessage", "Error fetching bill with ID: " + billId);
                return "viewAllBill"; // Redirect to the bill list page if the bill isn't found
            }
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Error fetching bill: " + e.getMessage());
            return "viewAllBill"; // Handle the error gracefully
        }
    }
    @PostMapping("/updateBill")
    public String updateBill(@ModelAttribute Bill bill, BindingResult result, Model model) {
        // Prepare the URL for the API call to update the bill
        String url = BASE_URL + "/api/bills/updateBill/" + bill.getBillId();

        try {
            // Send the bill data to the backend for update
            restTemplate.put(url, bill); // Assuming your backend API supports PUT request for updating the bill

            model.addAttribute("successMessage", "Bill updated successfully with ID: " + bill.getBillId());
            return "updateBillForm"; // Redirect to the update form with success message
        } catch (HttpClientErrorException e) {
            model.addAttribute("errorMessage", "Error occurred while updating the bill: " + e.getMessage());
            return "updateBillForm"; // Return to the form with error message
        }
    }

    @GetMapping("/paidbills/{patientId}")
    public String getPaidBillHistory(@PathVariable String patientId,  
                                     @RequestParam(required = false) LocalDate startDate, 
                                     @RequestParam(required = false) LocalDate endDate, 
                                     Model model) {
        // Construct the base URL for fetching paid bills
        String url = BASE_URL + "/api/bills/paidbills";
        System.out.println("jkshjkkoooo");

        // Create query parameters list
        List<String> queryParams = new ArrayList<>();
        queryParams.add("patientId=" + patientId); // Adding patientId as a required query parameter

        // Add startDate and endDate to query parameters if provided
        if (startDate != null) {
            queryParams.add("startDate=" + startDate);
        }
        if (endDate != null) {
            queryParams.add("endDate=" + endDate);
        }

        // Construct the full URL with query parameters
        if (!queryParams.isEmpty()) {
            url += "?" + String.join("&", queryParams);
        }

        System.out.println("Request URL: " + url); // Debugging log

        try {
            // Fetch the paid bills from the backend service
            ResponseEntity<List<Bill>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Bill>>() {}
            );

            List<Bill> paidBills = response.getBody();
            System.out.println("Fetched Paid Bills: " + paidBills); // Debugging log

            // Add paid bills to the model if they exist
            if (paidBills != null && !paidBills.isEmpty()) {
                model.addAttribute("paidBills", paidBills);
            } else {
                model.addAttribute("errorMessage", "No paid bills found for the given criteria.");
            }
        } catch (HttpClientErrorException e) {
            // Handle HTTP client errors (e.g., 404, 400)
            String errorMessage = "Error fetching paid bills: " + e.getMessage();
            model.addAttribute("errorMessage", errorMessage);
        } catch (Exception e) {
            // Handle unexpected errors (e.g., network issues)
            model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }

        return "paidBillHistory"; // Render the "paidBillHistory.html" template
    }
    
   

        @GetMapping("/patients/pending")
        public String viewUnpaidBills(@RequestParam(required = false) String patientId,
                                      @RequestParam(required = false) LocalDate startDate,
                                      @RequestParam(required = false) LocalDate endDate,
                                      Model model) {

            // Construct URL with query parameters
            String apiUrl = BASE_URL +"/api/bills/patients/pending"; // Preserve original BASE_URL
            System.out.println(apiUrl);
            List<String> queryParams = new ArrayList<>();
            if (patientId != null) {
                queryParams.add("patientId=" + patientId);
            }
            if (startDate != null && endDate != null) {
                queryParams.add("startDate=" + startDate);
                queryParams.add("endDate=" + endDate);
            }

            if (!queryParams.isEmpty()) {
                apiUrl += "?" + String.join("&", queryParams);
            }

            try {
                // Call REST API to fetch unpaid bills
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    apiUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );

                List<Map<String, Object>> bills = response.getBody();
                if (bills != null && !bills.isEmpty()) {
                    model.addAttribute("bills", bills);
                } else {
                    model.addAttribute("errorMessage", "No unpaid bills found with the provided criteria.");
                }
            } catch (HttpClientErrorException e) {
                // Handle HTTP errors
                String errorMessage = "Error fetching bills: " + e.getStatusCode();
                model.addAttribute("errorMessage", errorMessage);
            } catch (Exception e) {
                // Handle unexpected errors
                model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            }

            return "viewUnpaidBills"; // Render Thymeleaf template
        }
    

    
   
}





