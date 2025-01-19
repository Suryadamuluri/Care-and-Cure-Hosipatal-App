package com.hms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import jakarta.mail.util.ByteArrayDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	 @Autowired
    private  JavaMailSender emailSender;
	 @Autowired
	    private JavaMailSender mailSender;

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }
    // Method to send email notification
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("saran94422@gmail.com"); // Replace with your email or use environment variables
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
        System.out.println("Mail sent successfully to " + to);
    }
    public void sendEmailHtmlFormatt(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // 'true' indicates multipart message

        try {
            helper.setFrom("vinudevit@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true means HTML content is set

            emailSender.send(message);
            System.out.println("Email sent successfully!");

        } catch (MailException e) {
            e.printStackTrace();
            System.out.println("Error sending email: " + e.getMessage());
        }
    }
    public void sendEmailHtmlFormat(String to, String subject, String text, byte[] attachmentData, String attachmentFilename) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true); // Enable multipart

        try {
            helper.setFrom("vinudevit@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            // Use ByteArrayDataSource for the PDF attachment
            ByteArrayDataSource dataSource = new ByteArrayDataSource(attachmentData, "application/pdf");
            helper.addAttachment(attachmentFilename, dataSource);

            emailSender.send(message);
            System.out.println("Email sent successfully with PDF attachment!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error sending email with attachment: " + e.getMessage());
        }
    }
  /*  public void sendPaymentConfirmation(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("21b01a0581@svecw.edu.in");
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        System.out.println("Mail sent successfully...");
    }
    public void notifyPatientAdded(String patientEmail, String patientName) {
        String subject = "Patient Added: " + patientName;
        String text = "Dear Team,\n\nA new patient has been added.\n\nPatient Name: " + patientName +
                      "\n\nPlease review the patient details in the system.";
        sendEmail(patientEmail, subject, text);
    }

    // New method to notify when a patient is updated
    public void notifyPatientUpdated(String patientEmail, String patientName) {
        String subject = "Patient Updated: " + patientName;
        String text = "Dear Team,\n\nThe details of the patient have been updated.\n\nPatient Name: " + patientName +
                      "\n\nPlease review the updated patient information in the system.";
        sendEmail(patientEmail, subject, text);
    }*/


    //for doctorapp

//        public void sendEmail(String to, String subject, String text) {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(text);
//            mailSender.send(message);
//        }


}

