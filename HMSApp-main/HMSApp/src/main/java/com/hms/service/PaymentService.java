package com.hms.service;

import com.hms.entities.Bill;
import com.hms.entities.Payment;
import com.hms.entities.Patient;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.BillRepository;
import com.hms.repository.PaymentRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepo;
    private final EmailService emailService;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, BillRepository billRepo, EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.billRepo = billRepo;
        this.emailService = emailService;
    }
    
    public List<Payment> getPaymentsByPatientId(int patientId) {
        return paymentRepository.findByBillObj_Appointment_PatientObj_PatientId(patientId);
    }
    

    @Transactional
    public Payment addPayment(Payment payment, int billId) throws InvalidEntityException {
        Bill bill = billRepo.findById(billId)
                .orElseThrow(() -> new InvalidEntityException("Bill with ID " + billId + " not found"));
        System.out.println(bill);
        if (bill.getAppointment() != null) {
            System.out.println("Appointment ID: " + bill.getAppointment().getAppointmentId());
        } else {
            System.out.println("No Appointment associated with the Bill.");
        }

        payment.setBillObj(bill);
        System.out.println(bill);
        return paymentRepository.save(payment);
    }
    public void sendPaymentConfirmationEmail(Payment savedPayment) {
        Bill bill = savedPayment.getBillObj();
        if (bill != null && bill.getAppointment() != null && bill.getAppointment().getPatientObj() != null) {
            Patient patient = bill.getAppointment().getPatientObj();
            String emailBody = "Dear " + patient.getPatientName() + ",\n\n" +
                    "Thank you for your payment of $" + savedPayment.getAmountPaid() + ".\n" +
                    "Transaction ID: " + savedPayment.getTransactionId() + "\n" +
                    "Payment Date: " + savedPayment.getPaymentDate() + "\n" +
                    "Payment Method: " + savedPayment.getPaymentMethod() + "\n\n" +
                    "Regards,\nCare and Cure Hospital";

            emailService.sendEmail(patient.getEmailId(), "Payment Confirmation", emailBody);
        }
    }


   
  
    public List<Payment> filterPayments(String paymentStatus, LocalDate paymentDateFrom, LocalDate paymentDateTo,
            Double minAmount, Double maxAmount, String paymentMethod) {
// Get all payments first
List<Payment> payments = paymentRepository.findAll();

// Apply filters based on available parameters
if (paymentStatus != null && !paymentStatus.isEmpty()) {
payments = payments.stream()
  .filter(p -> p.getPaymentStatus().equals(paymentStatus))
  .collect(Collectors.toList());
}

if (paymentDateFrom != null) {
payments = payments.stream()
  .filter(p -> !p.getPaymentDate().isBefore(paymentDateFrom))
  .collect(Collectors.toList());
}

if (paymentDateTo != null) {
payments = payments.stream()
  .filter(p -> !p.getPaymentDate().isAfter(paymentDateTo))
  .collect(Collectors.toList());
}

if (minAmount != null && minAmount > 0) {
payments = payments.stream()
  .filter(p -> p.getAmountPaid() >= minAmount)
  .collect(Collectors.toList());
}

if (maxAmount != null && maxAmount > 0) {
payments = payments.stream()
  .filter(p -> p.getAmountPaid() <= maxAmount)
  .collect(Collectors.toList());
}

if (paymentMethod != null && !paymentMethod.isEmpty()) {
payments = payments.stream()
  .filter(p -> p.getPaymentMethod().equalsIgnoreCase(paymentMethod))
  .collect(Collectors.toList());
}

return payments;
}

    

  


    // View payment by ID
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found with ID: " + id));
    }

    // View all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public boolean isPaymentIdUnique(Long paymentId) {
        return paymentRepository.findById(paymentId).isEmpty(); // Assuming you have a PaymentRepository
    }

    public Payment updatePayment(Long id, Payment updatedPayment) {
        Optional<Payment> existingPaymentOpt = paymentRepository.findById(id);
        
        if (existingPaymentOpt.isPresent()) {
            Payment existingPayment = existingPaymentOpt.get();

            // Update fields
            existingPayment.setTransactionId(updatedPayment.getTransactionId());
            existingPayment.setPaymentDate(updatedPayment.getPaymentDate());
            existingPayment.setAmountPaid(updatedPayment.getAmountPaid());
            existingPayment.setPaymentMethod(updatedPayment.getPaymentMethod());
            existingPayment.setPaymentStatus(updatedPayment.getPaymentStatus());

            // If there are relationships (e.g., billObj), update those too
            if (updatedPayment.getBillObj() != null) {
                existingPayment.setBillObj(updatedPayment.getBillObj());
            }

            // Save the updated entity
            return paymentRepository.save(existingPayment); // Commit the update
        } else {
            throw new RuntimeException("Payment not found");
        }
    }



    // Delete payment by ID
    public void deletePayment(Long id) {
        if (paymentRepository.existsById(id)) {
            paymentRepository.deleteById(id);
        } else {
            throw new RuntimeException("Payment not found with ID: " + id);
        }
    }

    // Getters for repositories (if needed in some other part of your service)
    public PaymentRepository getPaymentRepository() {
        return paymentRepository;
    }

    public BillRepository getBillRepo() {
        return billRepo;
    }
}
