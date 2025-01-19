package com.hms.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hms.entities.Appointment;
import com.hms.entities.Bill;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;

import jakarta.mail.MessagingException;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private AppointmentRepository appRepository;
    @Autowired
    private EmailService emailService;
    public List<Bill> getPaidBillsByPatient(String patientId, LocalDate startDate, LocalDate endDate) {
        // If no date range is provided, use the default date range (before the current date)
        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = LocalDate.MIN;  // Represents the earliest possible date
            endDate = today;
        }
        return billRepository.findPaidBillsByPatientAndDateRange(patientId, startDate, endDate);
    }
    public Bill generateAndSendBill(Bill bill, int appointmentId) throws MessagingException, InvalidEntityException, IOException {
        // Check if a bill already exists for the appointment
        if (billExistsForAppointment(appointmentId)) {
            throw new InvalidEntityException("Bill already generated for this appointment ID");
        }

        // Set discount if applicable
        float discountPercentage = bill.getMedicineFees() > 1000 ? 10.0f : 0.0f;

        // Fetch consultation fees and calculate the total amount
        double consultationFees = getConsultationFees(appointmentId);
        double totalAmount = calculateTotalAmount(consultationFees, bill, discountPercentage);

        // Set bill details
        bill.setTotalAmount(totalAmount);
        bill.setDiscountPercentage(discountPercentage);
        bill.setTaxes(18);

        // Generate and save the bill
        Bill generatedBill = generateBill(bill, appointmentId);

        if (generatedBill != null) {
            // Send email with the bill summary
            sendBillEmailHtmlFormat(generatedBill, totalAmount);
        }

        return generatedBill;
    }
    public Bill generateBill(Bill bill, int appointmentId) throws InvalidEntityException {
        // Fetch the appointment based on appointmentId
        Appointment appointment = appRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidEntityException("Appointment not found for ID: " + appointmentId));

        // Set the appointment and other details in the bill
        bill.setAppointment(appointment); // Link appointment with the bill
        bill.setBillDate(LocalDate.now()); // Set bill date
        
        // Save the bill in the repository
        return billRepository.save(bill);
    }

    public List<Bill> getFilteredBills(Integer billId, LocalDate startDate, LocalDate endDate) throws InvalidEntityException {
        // Validation logic
        LocalDate today = LocalDate.now();

        if (startDate != null && startDate.isAfter(today)) {
            throw new IllegalArgumentException("Start date cannot be in the future.");
        }

        if (endDate != null && endDate.isAfter(today)) {
            throw new IllegalArgumentException("End date cannot be in the future.");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date.");
        }

        // Fetching logic
        if (billId != null) {
            Bill bill = getBillById(billId); // This already throws InvalidEntityException if bill is not found
            return List.of(bill); // Wrap the single bill in a list
        } else if (startDate != null && endDate != null) {
            return getBillsByDateRange(startDate, endDate); // Fetch bills within date range
        } else {
            return getAllBills(); // Fetch all bills
        }
    }
    public List<Bill> getPaidBillsByPeriod(LocalDate startDate, LocalDate endDate) {
        // If both start and end dates are provided, fetch the paid bills for the period
        if (startDate != null && endDate != null) {
            return billRepository.findPaidBillsByPeriod(startDate, endDate);
        }
        // If no date range is provided, return an empty list or handle as needed
        return new ArrayList<>(); // or you can throw an exception or return all bills, based on your requirement
    }
    private void sendBillEmailHtmlFormat(Bill generatedBill, double totalAmount) throws MessagingException, IOException {
        String patientEmail = generatedBill.getAppointment().getPatientObj().getEmailId();
        String patientName = generatedBill.getAppointment().getPatientObj().getPatientName();
        int appointmentId = generatedBill.getAppointment().getAppointmentId();

        String subject = "Bill Generated for Appointment " + appointmentId;

        // Generate PDF
        byte[] pdfBytes = generatePdfWithPDFBox(generatedBill, totalAmount, patientName, appointmentId);

        // Send Email with PDF Attachment
        emailService.sendEmailHtmlFormat(
            patientEmail,
            subject,
            "Please find your bill attached as a PDF.",
            pdfBytes,
            "Bill_Summary_" + appointmentId + ".pdf"
        );

        System.out.println("Email sent successfully to " + patientEmail);
    }

    private byte[] generatePdfWithPDFBox(Bill bill, double totalAmount, String patientName, int appointmentId) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Add a page
            PDPage page = new PDPage(PDRectangle.A4); // Use A4 page size for better formatting
            document.addPage(page);

            // Create content stream
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setLineWidth(1f); // Set line width for table borders

                // Title Section
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                contentStream.setLeading(22);
                contentStream.newLineAtOffset(50, 800); // Start a bit lower to create space for header
                contentStream.showText("Bill Summary");
                contentStream.newLine();
                contentStream.endText(); // End title text block

                // Patient and Appointment Details
                contentStream.beginText(); // Ensure beginText is called before newLineAtOffset
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, 750); // Position for patient details
                contentStream.showText("Patient Name: " + patientName);
                contentStream.newLine();
                contentStream.showText("Appointment ID: " + appointmentId);
                contentStream.newLine();
                contentStream.endText(); // End patient details text block

                // Table Header
                contentStream.beginText(); // Ensure beginText is called before newLineAtOffset
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.newLineAtOffset(50, 700); // Position for table header
                contentStream.showText("Details:");
                contentStream.newLine();
                contentStream.endText(); // End table header text block

                // Draw the table headers
                float yStart = 650;
                float tableWidth = 500;
                float yPosition = yStart;
                float rowHeight = 20f;

                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.beginText(); // Ensure beginText is called before newLineAtOffset
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("");
                contentStream.newLine();// Position for table headers
                contentStream.showText("Consultation Fees");
                contentStream.newLine();
                contentStream.showText("Medicine Fees");
                contentStream.newLine();
                contentStream.showText("Test Charges");
                contentStream.newLine();
                contentStream.showText("Miscellaneous Charges");
                contentStream.newLine();
                contentStream.showText("Discount (%)");
                contentStream.newLine();
                contentStream.showText("Taxes (%)");
                contentStream.newLine();
                contentStream.showText("Total Amount");
                contentStream.endText(); // End table header text block

                yPosition -= rowHeight;

                // Reset the y position for table content
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.beginText(); // Ensure beginText is called before newLineAtOffset
                contentStream.newLineAtOffset(200, yPosition); // Position for table content

                // Add the data to the table
                contentStream.showText(String.valueOf(bill.getConsultationFees()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(bill.getMedicineFees()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(bill.getTestCharge()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(bill.getMiscellaneousCharge()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(bill.getDiscountPercentage()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(bill.getTaxes()));
                contentStream.newLine();
                contentStream.showText(String.valueOf(totalAmount));
                contentStream.endText(); // End table content text block
            }

            // Save document to byte array
            document.save(baos);
            return baos.toByteArray();
        }
    }


    private void sendBillEmailHtmlFormatt(Bill generatedBill, double totalAmount) throws MessagingException {
        String patientEmail = generatedBill.getAppointment().getPatientObj().getEmailId();
        String patientName = generatedBill.getAppointment().getPatientObj().getPatientName();
        int appointmentId = generatedBill.getAppointment().getAppointmentId();

        String subject = "Bill Generated for Appointment " + appointmentId;
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head><title>Bill Summary</title></head>" +
                "<body>" +
                "<h2>Dear " + patientName + ",</h2>" +
                "<p>Your bill has been successfully generated for your recent appointment. Below are the details:</p>" +
                "<h3>Bill Summary:</h3>" +
                "<table border='1' cellpadding='10' cellspacing='0'>" +
                "<tr><th>Consultation Fees</th><td>" + generatedBill.getConsultationFees() + "</td></tr>" +
                "<tr><th>Medicine Fees</th><td>" + generatedBill.getMedicineFees() + "</td></tr>" +
                "<tr><th>Test Charges</th><td>" + generatedBill.getTestCharge() + "</td></tr>" +
                "<tr><th>Miscellaneous Charges</th><td>" + generatedBill.getMiscellaneousCharge() + "</td></tr>" +
                "<tr><th>Discount (%)</th><td>" + generatedBill.getDiscountPercentage() + "</td></tr>" +
                "<tr><th>Taxes (%)</th><td>" + generatedBill.getTaxes() + "</td></tr>" +
                "<tr><th><strong>Total Amount</strong></th><td><strong>" + totalAmount + "</strong></td></tr>" +
                "</table>" +
                "<p>Thank you for choosing our services. If you have any questions, feel free to reach out to us.</p>" +
                "<p>Best regards,<br>Your Hospital Name</p>" +
                "</body>" +
                "</html>";

        emailService.sendEmailHtmlFormatt(patientEmail, subject, body);
    }
    public Bill updateBill(Bill bill) {
        // Check if the bill exists in the database
        if (billRepository.existsById(bill.getBillId())) {
            // Update the bill in the database
            return billRepository.save(bill);
        } else {
            // If the bill doesn't exist, return null or throw an exception as needed
            return null;
        }
    }
    public double calculateTotalAmount(double consultationFees, Bill bill, float discountPercentage) {
        // Calculate the total amount before applying discount and taxes
        double totalAmount = consultationFees +
                bill.getMedicineFees() +
                bill.getTestCharge() +
                bill.getMiscellaneousCharge();

        // Apply discount if percentage is greater than 0
        if (discountPercentage > 0) {
            totalAmount -= totalAmount * (discountPercentage / 100);
        }

        // Apply a fixed tax rate of 18%
        double taxAmount = totalAmount * 0.18; // 18% tax
        totalAmount += taxAmount;

        return totalAmount;
    }


    // Get all bills
    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }
    public List<Bill> getBillsByDateRange(LocalDate startDate, LocalDate endDate) {
        return billRepository.findByBillDateBetween(startDate, endDate); // Fetch bills within the date range
    }
    public Bill getBillById(int billId) throws InvalidEntityException {
        return billRepository.findById(billId).orElseThrow(() -> new InvalidEntityException("Bill with ID " + billId + " does not exist."));
    }
    public List<Bill> getPendingBillsForPatient(String patientId) {
        List<Object[]> results = billRepository.findBillsWithPendingPaymentsByPatientId(patientId);
        List<Bill> bills = new ArrayList<>();

        for (Object[] result : results) {
            Bill bill = (Bill) result[0];
            Double totalPaid = (Double) result[1];
            bill.setTotalPaid(totalPaid != null ? totalPaid : 0.0); // Set the transient field
            bills.add(bill);
        }

        return bills;
    }
    public Bill updateMedicineFees(int billId, double medicineFees) throws InvalidEntityException {
        // Use the findById method that throws InvalidEntityException if the bill is not found
        Bill bill = getBillById(billId);
        bill.setMedicineFees(medicineFees);  // Update the medicine fees
        return billRepository.save(bill);  // Save and return the updated bill
    }
    public boolean billExistsForAppointment(int appointmentId) {
        // Retrieve the appointment based on the appointmentId
        Optional<Appointment> appointmentOptional = appRepository.findById(appointmentId);

        // If the appointment exists, check if a bill is already linked to it
        if (appointmentOptional.isPresent()) {
            Appointment appointment = appointmentOptional.get();
            // Check if a bill exists for this appointment
            Optional<Bill> existingBill = billRepository.findByAppointment(appointment);
            return existingBill.isPresent(); // Return true if a bill exists, false otherwise
        }
        // Return false if the appointment does not exist
        return false;
    }

  
    public double getConsultationFees(int appointmentId) throws InvalidEntityException {
        // Retrieve the appointment from the repository using the appointmentId
        Appointment appointment = appRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidEntityException("Appointment not found for ID: " + appointmentId));

        // If found, get the consultation fees from the associated doctor
        return appointment.getDoctorObj().getConsultationFees();
    }
    // Update test charges for a bill
    public Bill updateTestCharge(int billId, double testCharge) throws InvalidEntityException {
    	   Bill bill = getBillById(billId);
    	   bill.setTestCharge(testCharge);  // Update the medicine fees
           return billRepository.save(bill);
    }
    
    
    //unpaidbillforallpateint
    
    public List<Map<String, Object>> getUnpaidBills(String patientId, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = billRepository.findUnpaidBills(patientId, startDate, endDate);

        List<Map<String, Object>> unpaidBills = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> billData = new HashMap<>();
            Bill bill = (Bill) result[0];
            String patientName = (String) result[1];
            Double totalAmount = (Double) result[2];
            LocalDate billDate = (LocalDate) result[3];
            Double totalPaid = (Double) result[4];

            // Calculate the remaining amount
            Double remainingAmount = totalAmount - totalPaid;

            billData.put("billId", bill.getBillId());
            billData.put("patientName", patientName);
            billData.put("totalAmount", totalAmount);
            billData.put("billDate", billDate);
            billData.put("totalPaid", totalPaid);
            billData.put("remainingAmount", remainingAmount);

            unpaidBills.add(billData);
        }

        return unpaidBills;
    }
    public double calculateTotalAmountt( Bill bill) {
        // Calculate the total amount before applying discount and tax
        double totalAmount = bill.getConsultationFees() +
                bill.getMedicineFees() +
                bill.getTestCharge() +
                bill.getMiscellaneousCharge(); // Ensure this field exists in Bill

        // Apply discount if percentage is provided (greater than 0)
        double discountPercentage = bill.getDiscountPercentage();
        if (discountPercentage > 0) {
            totalAmount -= totalAmount * (discountPercentage / 100);
        }

        // Apply a fixed tax rate of 18%
        double taxAmount = totalAmount * 0.18; // 18% tax
        totalAmount += taxAmount;

        return totalAmount;
    }


}
