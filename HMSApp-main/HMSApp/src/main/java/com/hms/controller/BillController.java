package com.hms.controller;

import com.hms.entities.Appointment;
import com.hms.entities.Bill;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.BillRepository;
import com.hms.service.BillService;
import com.hms.service.EmailService;

import ch.qos.logback.core.model.Model;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;

import com.hms.service.AppointmentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;
    @Autowired
    private BillRepository billrepo;
    @GetMapping("/paidbills")
    public List<Bill> getPaidBills(@RequestParam String patientId,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return billService.getPaidBillsByPatient(patientId, startDate, endDate);
    }
    @GetMapping("/{billId}")
    public ResponseEntity<?> getBillById(@PathVariable int billId) {
        try {
            // Fetch the bill details
            Bill bill = billService.getBillById(billId);
            
            if (bill != null) {
                // Return HTTP 200 (OK) with the bill details
                return ResponseEntity.ok(bill);
            } else {
                // Return HTTP 404 (Not Found) if the bill is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "message", "Bill not found",
                                "billId", billId
                        ));
            }
        } catch (Exception ex) {
            // Log the exception and return HTTP 500 (Internal Server Error)
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "message", "An error occurred while fetching the bill details",
                            "error", ex.getMessage()
                    ));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Bill> updateBill(@PathVariable int id, @RequestBody Bill bill) {
        // Check if the bill with the given id exists
        Optional<Bill> existingBillOptional = billrepo.findById(id);
        
        if (!existingBillOptional.isPresent()) {
            // If the bill is not found, return a Not Found (404) response
            return ResponseEntity.notFound().build();
        }

        Bill existingBill = existingBillOptional.get();

        // Update the properties of the existing bill
        existingBill.setTotalAmount(bill.getTotalAmount());
        existingBill.setStatus(bill.getStatus());
     
        // Add any other fields you need to update

        // Save the updated bill
        Bill updatedBill = billrepo.save(existingBill);

        // Return the updated bill with a 200 OK response
        return ResponseEntity.ok(updatedBill);
    }
    @PostMapping("/generateBill/{appointmentId}")
    public ResponseEntity<Object> generateBill(
            @Valid @RequestBody Bill bill,
           
            @PathVariable int appointmentId) throws MessagingException, InvalidEntityException, IOException  {

      
          
            Bill generatedBill = billService.generateAndSendBill(bill, appointmentId);

            if (generatedBill != null) {
                return ResponseEntity.status(HttpStatus.CREATED).body(generatedBill);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if generation failed
            }
    }

    @GetMapping("/viewAllBills")
    public ResponseEntity<Object> viewAllBills(
            @RequestParam(required = false) Integer billId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        try {
            List<Bill> bills = billService.getFilteredBills(billId, startDate, endDate);
            if (bills.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build(); // Return 204 if no bills found
            }
            return ResponseEntity.status(HttpStatus.OK).body(bills); // Return 200 with the list of bills
        } catch (InvalidEntityException ex) {
           
            Map<String, String> error = Map.of("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error); // Return 400 with the error message
        } catch (IllegalArgumentException ex) {
           
            Map<String, String> error = Map.of("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error); // Return 400 with the error message
        } catch (Exception ex) {
           
            Map<String, String> error = Map.of("message", "An unexpected error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    @GetMapping("/patient/{patientId}/pending")
    public List<Bill> getPendingBills(@PathVariable String patientId) {
        return billService.getPendingBillsForPatient(patientId);
    }
    
    @GetMapping("/paid")
    public ResponseEntity<List<Bill>> getPaidBillsByPeriod(
            @RequestParam(name = "startDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate", required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // Set default date range if dates are not provided
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1); // Default to last month
        }
        if (endDate == null) {
            endDate = LocalDate.now(); // Default to today
        }

        // Optional: Validate date range
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body(null); // Return 400 if the date range is invalid
        }

        List<Bill> paidBills = billService.getPaidBillsByPeriod(startDate, endDate);

        // If no bills found, return a 204 No Content response
        if (paidBills.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        // Return a 200 OK response with the list of paid bills
        return ResponseEntity.ok(paidBills);
    }
   

    
    // Get a specific bill by ID
    @GetMapping("/viewBill/{billId}")
    public ResponseEntity<Bill> viewBill(@PathVariable int billId) throws InvalidEntityException {
        Bill bill = billService.getBillById(billId);
        if (bill != null) {
            // Return HTTP 200 (OK) with the Bill details
            return ResponseEntity.status(HttpStatus.OK).body(bill);
        } else {
            // Return HTTP 404 (Not Found) if the Bill is not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
  
    @PutMapping("/updateMedicineFees/{billId}/{medicineFees}")
    public ResponseEntity<Object> updateMedicineFees(@PathVariable int billId, @PathVariable double medicineFees) throws InvalidEntityException {
        try {
            Bill updatedBill = billService.updateMedicineFees(billId, medicineFees);
            
            if (updatedBill != null) {
                // Return HTTP 200 (OK) with the updated Bill
                return ResponseEntity.status(HttpStatus.OK).body(updatedBill);
            } else {
                // Return HTTP 404 (Not Found) if the Bill is not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (InvalidEntityException ex) {
            // Create an error map to capture the error message
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());

            // Return the error message with NOT_FOUND status
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }

   
    // Update test charges for a bill
    @PutMapping("/updateTestCharge/{billId}/{testCharge}")
    public ResponseEntity<Object> updateTestCharge(@PathVariable int billId, @PathVariable double testCharge) throws InvalidEntityException {
        Bill updatedBill;
		try {
			updatedBill = billService.updateTestCharge(billId, testCharge);
			if (updatedBill != null) {
	            // Return HTTP 200 (OK) with the updated Bill
	            return ResponseEntity.status(HttpStatus.OK).body(updatedBill);
	        } else {
	          
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	        }
		} catch (InvalidEntityException ex) {
			// TODO Auto-generated catch block
			Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());

            // Return the error message with NOT_FOUND status
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
		}
        
    }

    @GetMapping("/consultationFees/{appointmentId}")
    public ResponseEntity<Object> getConsultationFees(@PathVariable int appointmentId) throws InvalidEntityException {
        try {
            // Call service to fetch consultation fee
            double consultationFees = billService.getConsultationFees(appointmentId);

            // Return consultation fee with HTTP status OK
            return ResponseEntity.ok(consultationFees);
        } catch (InvalidEntityException ex) {
            // Create an error map to capture the message
            Map<String, String> error = new HashMap<>();
            error.put("message", ex.getMessage());

            // Return the error message with NOT_FOUND status
            return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/updateBill/{billId}")
    public ResponseEntity<Bill> updateBill(@PathVariable("billId") Integer billId, @RequestBody Bill bill) throws InvalidEntityException {
        // Find the existing bill by ID
        Bill existingBill = billService.getBillById(billId);

        if (existingBill == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Update the bill fields from the request body
        existingBill.setDescription(bill.getDescription()); // Keep description unchanged
        existingBill.setConsultationFees(bill.getConsultationFees());
        existingBill.setMedicineFees(bill.getMedicineFees());
        existingBill.setTestCharge(bill.getTestCharge());

        // Apply discount if the medicine fees exceed 1000
        if (existingBill.getMedicineFees() > 1000) {
            existingBill.setDiscountPercentage(10.0f); // Set discount to 10% if medicine fees exceed 1000
        }

        // Recalculate the total amount after applying the discount
        double totalAmount = billService.calculateTotalAmountt(
           
            existingBill // Pass the updated bill object
        );
        existingBill.setTotalAmount(totalAmount); // Update the total amount

        // Save the updated bill
        Bill updatedBill = billService.updateBill(existingBill);

        return ResponseEntity.ok(updatedBill);
    }

	//unpaidall patient
    @GetMapping("/patients/pending")
    public ResponseEntity<List<Map<String, Object>>> getUnpaidBills(
        @RequestParam(required = false) String patientId,
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        List<Map<String, Object>> unpaidBills = billService.getUnpaidBills(patientId, startDate, endDate);

        if (!unpaidBills.isEmpty()) {
            return ResponseEntity.ok(unpaidBills);
        } else {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
    }


    }


