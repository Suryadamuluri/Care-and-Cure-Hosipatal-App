package com.hms.controller;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;

import org.springframework.http.HttpStatus;

import com.hms.entities.Appointment;
import com.hms.entities.Bill;
import com.hms.entities.Patient;
import com.hms.entities.Payment;
import com.hms.entities.UserInfo;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PaymentUIController {

    private static final String PAYMENTS_API_URL = "http://localhost:7220/api/payments";  // Example API URL

    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/home")
    public String showHomePage( Model model) {
        return "home"; // Ensure there's a `home.html` in your templates folder.
    }
    
    @GetMapping("/doctor/profile")
    public String showDoctorProfileForm() {
        return "DoctorProfile";  // The form where user enters Doctor ID
    }

    // Frontend method for displaying doctor revenue
    @GetMapping("/doctor/revenue")
    public String showDoctorRevenue(@RequestParam("doctorId") int doctorId, Model model) {
        // Call the backend API to get the revenue breakdown
        String url = "http://localhost:7220/api/payments/revenue-breakdown?doctorId=" + doctorId;
        Map<String, Object> revenueData = restTemplate.getForObject(url, Map.class);

        // Extract total revenue and breakdown details
        double totalRevenue = (double) revenueData.get("totalRevenue");
        List<Map<String, Object>> breakdown = (List<Map<String, Object>>) revenueData.get("breakdown");

        // Add data to the model
        model.addAttribute("doctorId", doctorId);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("breakdown", breakdown);
        System.out.println("Revenue Breakdown: " + breakdown);

        // Return the view for displaying revenue
        return "doctorRevenue";
    }

    
    
    
    

    // ******************** USER PROFILE ********************

    // User Profile - Show Payment Page
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

    @GetMapping("/patient/payments")
    public String viewPaymentsByPatient(@RequestParam(value="patientId",required=false) Integer patientId, Model model) {
        // Call your service or API to get payments data for this patient
    	System.out.println(role+" "+patSession);
    	if(role!=null && role.equals("patient") && patientId==null && patSession!=null) {
    		patientId = patSession.getPatientId();
    	}
    	else if(role.equals("patient") && patientId==null && patSession==null) 
        {
        	model.addAttribute("userInfo", new UserInfo());
            return "login";
        }
    	
        ResponseEntity<Payment[]> response = restTemplate.exchange(
            "http://localhost:7220/api/payments/patient/" + patientId,
            HttpMethod.GET,
            null,
            Payment[].class
        );

        // Add the response data (payments) to the model
        model.addAttribute("payments", response.getBody());

        // Return the name of the Thymeleaf template (paymentList)
        return "paymentList";  // this is the HTML template to be rendered
    }

    @GetMapping("/make-payment")
    public String showMakePaymentPage(@RequestParam("billId") Long billId,
                                       @RequestParam("totalAmount") Double totalAmount,
                                       @RequestParam("totalPaid") Double totalPaid,
                                       @RequestParam("outstandingAmount") Double outstandingAmount,
                                       Model model) {
        // Use the passed data directly without fetching from the backend
        model.addAttribute("billId", billId);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("paymentAmount", outstandingAmount); // Outstanding amount

        return "MakePayment"; // Redirect to the Thymeleaf template
    }

    
 /*  @GetMapping("/make-payment")
    public String showMakePaymentPage(@RequestParam("billId") Long billId, Model model) {
        // Call the Bill API to fetch the details
        String url = "http://localhost:7220/api/bills/" + billId; // Replace with your actual API
        Bill bill = restTemplate.getForObject(url, Bill.class);

        if (bill == null) {
            model.addAttribute("error", "Bill not found!");
            return "errorPage"; // Redirect to an error page if bill is not found
        }

        // Pass bill details to the view
        model.addAttribute("bill", bill);
        model.addAttribute("billId", billId);
        model.addAttribute("totalAmount", bill.getTotalAmount()); // Assuming the total amount is in the Bill object
        model.addAttribute("paymentAmount", bill.getTotalAmount() - bill.getTotalPaid()); // Amount to be paid, which is the difference
        return "MakePayment"; // The name of the Thymeleaf template for the payment page
    }
*/

    @PostMapping("/submit-payment")
    public String submitPayment(@RequestParam("billId") Long billId,
                                @RequestParam("paymentAmount") Double paymentAmount,
                                @RequestParam("outstandingAmount") Double outstandingAmount,
                                @RequestParam("paymentMethod") String paymentMethod,
                                RedirectAttributes redirectAttributes) {
        // Log received parameters
        System.out.println("Received billId: " + billId);
        System.out.println("Received paymentAmount: " + paymentAmount);
        System.out.println("Received paymentMethod: " + paymentMethod);

        // Call the Bill API to fetch bill details
        String billUrl = "http://localhost:7220/api/bills/" + billId;
        Bill bill = restTemplate.getForObject(billUrl, Bill.class);

        if (bill == null || bill.getAppointment() == null || bill.getAppointment().getPatientObj() == null) {
            redirectAttributes.addFlashAttribute("error", "Bill or Patient information is missing!");
            return "redirect:/bills/pending";
        }

        int patientId = bill.getAppointment().getPatientObj().getPatientId();

        if (paymentAmount > outstandingAmount) {
            redirectAttributes.addFlashAttribute("error", "Payment amount cannot exceed the outstanding amount.");
            return "redirect:/make-payment?billId=" + billId + "&outstandingAmount=" + outstandingAmount;
        }

        // Determine payment status
        String paymentStatus = "Unpaid";
        if (paymentAmount.equals(outstandingAmount)) {
            paymentStatus = "Paid";
        } else if (paymentAmount < outstandingAmount) {
            paymentStatus = "Partially Paid";
        }

        // Create the Payment object
        Payment payment = new Payment();
        payment.setAmountPaid(paymentAmount);
        payment.setPaymentDate(LocalDate.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(paymentStatus);
        payment.setTransactionId("TXN" + System.currentTimeMillis());

        // Send payment details to the Payment API
        String paymentUrl = "http://localhost:7220/api/payments/submit-payment";
        MultiValueMap<String, String> paymentDetails = new LinkedMultiValueMap<>();
        paymentDetails.add("billId", String.valueOf(billId));
        paymentDetails.add("paymentAmount", String.valueOf(paymentAmount));
        paymentDetails.add("paymentMethod", paymentMethod);
        paymentDetails.add("paymentStatus", paymentStatus);
        paymentDetails.add("transactionId", payment.getTransactionId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(paymentDetails, headers);

        try {
            restTemplate.postForObject(paymentUrl, entity, Void.class);

            // Update the bill's total paid amount
            double updatedTotalPaid = bill.getTotalPaid() + paymentAmount;
            bill.setTotalPaid(updatedTotalPaid);
            restTemplate.put(billUrl, bill);

            redirectAttributes.addFlashAttribute("message", "Payment successful!");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while processing the payment. Please try again.");
            return "redirect:/patient/" + patientId + "/payments";
        }

        redirectAttributes.addFlashAttribute("message", "Payment of ₹" + paymentAmount + " was successful!");
        return "redirect:/patient/" + patientId + "/payments";
        
    }



 
    
    
    
    
  
    // ******************** ADMIN PROFILE ********************

    // ADMIN Profile - View all payments with filtering
    
 // Admin Profile - View all payments with filtering
    @GetMapping("/admin/payments/")
    public String showPayments(Model model) {
    	 PaymentFilter filter = new PaymentFilter(); // or retrieve from session or database
    	    model.addAttribute("filter", filter);
        try {
            // Fetch all payments from the backend as a list of Payment objects
            ResponseEntity<List<Payment>> response = restTemplate.exchange(
                PAYMENTS_API_URL + "/viewAllPayments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Payment>>() {}
            );
            List<Payment> payments = response.getBody();  // Get the list of payments
            model.addAttribute("payments", payments);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch payments. Please try again.");
            e.printStackTrace();
        }

        return "payments";  // Return the payments view (payments.html)
    }

    @GetMapping("/admin/payments/viewAll")
    public String viewAllPayments(@ModelAttribute("filter") PaymentFilter filter, Model model) {
        // Log the filter for debugging purposes
        System.out.println("Filter: " + filter);

        // Construct the URL for the filter API (assuming it's a GET request)
        StringBuilder url = new StringBuilder("http://localhost:7220/api/payments/viewAllPayments?");

        // Conditionally add parameters if they are not null or empty
        if (filter.getPaymentStatus() != null && !filter.getPaymentStatus().isEmpty()) {
            url.append("paymentStatus=" + filter.getPaymentStatus() + "&");
        }
        if (filter.getPaymentDateFrom() != null) {
            url.append("paymentDateFrom=" + filter.getPaymentDateFrom() + "&");
        }
        if (filter.getPaymentDateTo() != null) {
            url.append("paymentDateTo=" + filter.getPaymentDateTo() + "&");
        }
        if (filter.getMinAmount() != null) {
            url.append("minAmount=" + filter.getMinAmount() + "&");
        }
        if (filter.getMaxAmount() != null) {
            url.append("maxAmount=" + filter.getMaxAmount() + "&");
        }
        if (filter.getPaymentMethod() != null && !filter.getPaymentMethod().isEmpty()) {
            url.append("paymentMethod=" + filter.getPaymentMethod() + "&");
        }

        // Remove the trailing '&' if it exists
        if (url.charAt(url.length() - 1) == '&') {
            url.deleteCharAt(url.length() - 1);
        }

        // Make the API call to get filtered payments
        List<Payment> filteredPayments = restTemplate.exchange(url.toString(), HttpMethod.GET, null, 
                                                                new ParameterizedTypeReference<List<Payment>>() {}).getBody();

        // Add the filtered payments and filter to the model
        model.addAttribute("payments", filteredPayments);
        model.addAttribute("filter", filter);

        return "payments";  // Return the payments view
    }



    @GetMapping("/admin/payments/add")
    public String showAddPaymentForm(Model model) {
        Payment payment = new Payment();
        model.addAttribute("payment", payment);
        return "addPayment";
    }

    @PostMapping("/admin/payments/add")
    public String addPayment(@ModelAttribute("payment") Payment payment, Model model) {
        Payment savedPayment = restTemplate.postForObject(PAYMENTS_API_URL + "/add", payment, Payment.class);
        if (savedPayment == null) {
            model.addAttribute("error", "Failed to save payment.");
            return "addPayment";
        }
        return "redirect:/admin/payments/viewAll";
    }
 // In your Controller
    @GetMapping("/admin/payments/updatePayment/{id}")
    public String showUpdatePaymentForm(@PathVariable("id") int id, Model model) {
        Payment payment = restTemplate.getForObject(PAYMENTS_API_URL + "/getById/{id}", Payment.class, id);
        model.addAttribute("payment", payment);
        
        // Fetch the list of bills for the dropdown
        Bill[] bills = restTemplate.getForObject("http://localhost:7220/api/bills/viewAllBills", Bill[].class);
        model.addAttribute("bills", Arrays.asList(bills));

        return "editPayment";
    }

    @PostMapping("/admin/payments/updatePayment/{id}")
    public String updatePayment(@PathVariable("id") int id, @ModelAttribute("payment") Payment updatedPayment) {
        // Fetch the existing payment object
        Payment existingPayment = restTemplate.getForObject(PAYMENTS_API_URL + "/getById/{id}", Payment.class, id);

        // Only update if payment is not marked as "Paid"
        if ("Paid".equals(existingPayment.getPaymentStatus())) {
            existingPayment.setPaymentDate(updatedPayment.getPaymentDate());
            existingPayment.setAmountPaid(updatedPayment.getAmountPaid());
            existingPayment.setPaymentMethod(updatedPayment.getPaymentMethod());
            existingPayment.setPaymentStatus(updatedPayment.getPaymentStatus());
            existingPayment.setTransactionId(updatedPayment.getTransactionId());
        } else {
            // You may want to add some logic to alert the user
            // that payments with status "Paid" cannot be modified
            existingPayment.setAmountPaid(updatedPayment.getAmountPaid()); // Allow amount paid to be modified
        }

        // Update the associated bill if it has changed
        if (updatedPayment.getBillObj() != null) {
            existingPayment.setBillObj(updatedPayment.getBillObj());
        }

        // Send the updated payment object to the API
        restTemplate.put(PAYMENTS_API_URL + "/updatePayment/{id}", existingPayment, id);

        return "redirect:/admin/payments/";
    }


    @GetMapping("/admin/payments/delete/{paymentId}")
    public String deletePayment(@PathVariable("paymentId") Long paymentId, RedirectAttributes redirectAttributes) {
        // Fetch the payment to check its status
        Payment payment = restTemplate.getForObject(PAYMENTS_API_URL + "/getById/{id}", Payment.class, paymentId);
        
        if ("Paid".equals(payment.getPaymentStatus())) {
            redirectAttributes.addFlashAttribute("error", "Paid payments cannot be deleted.");
            return "redirect:/admin/payments/";
        }

        restTemplate.delete(PAYMENTS_API_URL + "/deletePayment/{paymentId}", paymentId);
        return "redirect:/admin/payments/";
    }
}
