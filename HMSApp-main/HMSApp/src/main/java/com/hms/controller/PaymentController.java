package com.hms.controller;

import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.hms.entities.Appointment;
import com.hms.entities.Bill;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;
import com.hms.entities.Payment;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.PaymentRepository;
import com.hms.service.BillService;
import com.hms.service.DoctorService;
import com.hms.service.PaymentService;
import com.hms.exception.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService service;
    
    @Autowired
    private BillService billService;
    
    @Autowired
    private BillRepository billRepository;
    
    
   
   
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

 
    
    @Autowired
    private PaymentRepository paymentRepository;

    // Backend logic for calculating doctor revenue (this could be called via AJAX or a normal GET request)
    @GetMapping("/revenue-breakdown")
    @ResponseBody
    public Map<String, Object> calculateDoctorRevenueWithBreakdown(@RequestParam("doctorId") Integer doctorId) {
        // Step 1: Fetch the doctor by ID
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid doctor ID"));

        // Step 2: Get all appointments for the doctor
        List<Appointment> appointments = appointmentRepository.findByDoctorObj_DoctorId(doctorId);

        // Step 3: Initialize revenue calculation
        double totalRevenue = 0.0;
        List<Map<String, Object>> appointmentBreakdown = new ArrayList<>();

        // Step 4: Calculate revenue per appointment
        for (Appointment appointment : appointments) {
            Bill bill = billRepository.findByAppointment_AppointmentId(appointment.getAppointmentId());
            if (bill != null) {
                // Fetch all payments for the bill using the newly created method
                List<Payment> payments = paymentRepository.findByBillObj_BillId(bill.getBillId());

                // Calculate total paid amount for this bill
                double paidAmount = payments.stream().mapToDouble(Payment::getAmountPaid).sum();

                // Accumulate the total revenue
                totalRevenue += paidAmount;

                // Prepare appointment breakdown
                Map<String, Object> appointmentData = new HashMap<>();
                appointmentData.put("appointmentId", appointment.getAppointmentId());
                appointmentData.put("patientId", appointment.getPatientObj().getPatientId());
                appointmentData.put("patientName", appointment.getPatientObj().getPatientName());
                appointmentData.put("billId", bill.getBillId());
                appointmentData.put("description", bill.getDescription());
                appointmentData.put("totalBillAmount", bill.getTotalAmount());
                appointmentData.put("paidAmount", paidAmount); // Total payments made
                appointmentData.put("remainingAmount", bill.getTotalAmount() - paidAmount);

                // Add the data to the breakdown list
                appointmentBreakdown.add(appointmentData);
            }
        }

        // Step 5: Prepare the response
        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue); // Sum of all paid amounts
        response.put("currency", "INR");
        response.put("breakdown", appointmentBreakdown); // Detailed breakdown

        return response; // Return as JSON
    }

    
    

    // Add a payment and send email notification
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Payment>> getPaymentsByPatientId(@PathVariable("patientId") int patientId) {
        List<Payment> payments = service.getPaymentsByPatientId(patientId);

        if (payments.isEmpty()) {
            return ResponseEntity.noContent().build(); // Return 204 if no payments found
        }
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/submit-payment")
    public ResponseEntity<Void> processPayment(@RequestParam int billId,
                                               @RequestParam Double paymentAmount,
                                               @RequestParam String paymentMethod,
                                               @RequestParam String transactionId,
                                               @RequestParam String paymentStatus) {
        try {
            System.out.println("Received billId: " + billId);
            System.out.println("Received paymentAmount: " + paymentAmount);
            System.out.println("Received paymentMethod: " + paymentMethod);
            System.out.println("Received transactionId: " + transactionId);
            System.out.println("Received paymentStatus: " + paymentStatus);

            Bill bill = billService.getBillById(billId);
            if (bill == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Create and save the payment
            Payment payment = new Payment();
            payment.setAmountPaid(paymentAmount);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentDate(LocalDate.now());
            payment.setPaymentStatus(paymentStatus);
            payment.setTransactionId(transactionId);
            service.addPayment(payment, billId);
            
            service.sendPaymentConfirmationEmail(payment);
            // Update the bill's total paid amount
            double updatedTotalPaid = bill.getTotalPaid() + paymentAmount;
            bill.setTotalPaid(updatedTotalPaid);
            billRepository.save(bill);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }




    @PostMapping("/add/{billId}")
    public ResponseEntity<?> addPayment(@Valid @RequestBody Payment payment, @PathVariable int billId)throws InvalidEntityException {
   
    	  
        if (payment.getPaymentId() != null && !service.isPaymentIdUnique(payment.getPaymentId())) {
            return new ResponseEntity<>("Payment ID already exists", HttpStatus.BAD_REQUEST);
        }

        // Add the payment if validation passes
      
            Payment savedPayment = service.addPayment(payment, billId);

            // Send email notification
          service.sendPaymentConfirmationEmail(savedPayment);

            // Return success response
            return ResponseEntity.ok("Payment added and email sent successfully!");  
        
    }




    @GetMapping("/viewAllPayments")
    public ResponseEntity<List<Payment>> viewAllPayments(
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) LocalDate paymentDateFrom,
            @RequestParam(required = false) LocalDate paymentDateTo,
            @RequestParam(required = false, defaultValue = "0") Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String paymentMethod) {  // Added paymentMethod filter
        try {
            // Filtering logic in the service
            List<Payment> payments = service.filterPayments(paymentStatus, paymentDateFrom, paymentDateTo, minAmount, maxAmount, paymentMethod);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            // Log the exception for debugging purposes
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ArrayList<>());
        }
    }




    // View a specific payment by ID
    @GetMapping("/getById/{id}")
    public ResponseEntity<Payment> viewPaymentById(@PathVariable Long id) {
        try {
            Payment payment = service.getPaymentById(id);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // Update an existing payment
    @PutMapping("/updatePayment/{id}")
    public ResponseEntity<?> updatePayment(
            @PathVariable Long id, 
            @Valid @RequestBody Payment updatedPayment, 
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errorMessages);
        }

        try {
            Payment payment = service.updatePayment(id, updatedPayment);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
        }
    }

    // Delete a payment by ID
    @DeleteMapping("/deletePayment/{id}")
    public ResponseEntity<String> deletePayment(@PathVariable Long id) {
        try {
            service.deletePayment(id);
            return ResponseEntity.ok("Payment deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found");
        }
    }
}
