package com.hms.service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Validator;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.hms.repository.AppointmentRepository;
import com.hms.repository.PatientRepository;
import com.hms.entities.Appointment;
import com.hms.entities.Patient;
import com.hms.entities.UserInfo;
import com.hms.exception.InvalidEntityException;

import org.springframework.stereotype.Service;

import com.hms.entities.Patient;
import com.hms.repository.PatientRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private EmailService emailService; // Autowired instance of EmailService

    @Autowired
    UserService userService;

    
    public Patient getPatientByUserName(String username) {
        Patient p =  patientRepository.findByUserUserName(username);
       
        return p;
    }

    public Patient addPatient(Patient patient) throws InvalidEntityException {
        patient.setStatus("Active");

        String temp = patient.getContactNumber().substring(6);//patient.getContactNumber().length()-4);
        UserInfo u = new UserInfo(patient.getPatientName().substring(0,3)+temp, "test", "patient");
        patient.setUser(u);
        userService.addUser(u);        
    	
        Patient newPatient = patientRepository.save(patient);

        // Notify patient via email
        emailService.sendEmail(
                patient.getEmailId(), // Assuming you have 'emailId' in Patient entity
                "Patient Registration Successful",
                "Dear " + patient.getPatientName() + ",\n\n" +
                        "You have been successfully registered in our system.\nYour id is " +patient.getPatientId()+
                        "your username and password are "+patient.getUser().getUserName()+", "+patient.getUser().getPassword()+"."+                    
                        "Thank you for choosing our services.\n\n" +
                        "Best Regards,\n"
                        + "Hospital Management Team"
        );

        return newPatient;
    }


	// View all patients
    public List<Patient> viewAllPatients() {
        try {
            return patientRepository.findAll();  // Retrieve all patients from the database
        } catch (Exception ex) {
            throw new RuntimeException("Error while retrieving patients: " + ex.getMessage());
        }
    }

    // Get a patient by ID
    public Patient getPatientById(int id) {
        try {
            Optional<Patient> patient = patientRepository.findById(id);  // Find patient by ID
            return patient.orElse(null);  // Return patient if found, otherwise null
        } catch (Exception ex) {
            throw new RuntimeException("Error while retrieving patient by ID: " + ex.getMessage());
        }
    }

    public List<Patient> getPatientByName(String patientName) {
        return patientRepository.findByPatientName(patientName);
}
    public List<Patient> getPatientsByMedicalHistory(String medicalHistory) {
        return patientRepository.findByMedicalHistory(medicalHistory);
    }
    

    public Patient updatePatient(Patient patient) {
        Patient updatedPatient = patientRepository.save(patient);
       
        String subject = "Patient Updated: " + patient.getPatientName();
        String text = "Dear Team,\n\nThe details of the patient have been updated.\n\nPatient Name: " + patient.getPatientName() +
                      "\n\nPlease review the updated patient information in the system.";
        emailService.sendEmail(patient.getEmailId(), subject, text);
        return updatedPatient;
    }
   
	
    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Patient> getPatientsByDoctorAndDate(int doctorId, LocalDate appointmentDate) {
        List<Patient> patients = appointmentRepository.findPatientsByDoctorAndDate(doctorId, appointmentDate);
        if (patients == null || patients.isEmpty()) {
            // Optionally, log or throw a custom exception
            return List.of(); // Return empty list if no patients found
        }
        return patients;
    }

    public void deactivateInactivePatients() {
        List<Patient> patients = patientRepository.findAllWithAppointments(); // Fetch patients with their appointments

        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);  // Date 2 years ago from today

        for (Patient patient : patients) {
            String isInactive = "INACTIVE";  // Default value is "INACTIVE"

            if (patient.getAppointmentList() != null && !patient.getAppointmentList().isEmpty()) {
                // Check if the patient has any appointment within the last 2 years
                boolean hasRecentAppointment = patient.getAppointmentList().stream()
                        .anyMatch(appointment -> appointment.getAppointmentDate().isAfter(twoYearsAgo));
                
                // If there's any recent appointment, set status to "ACTIVE"
                if (hasRecentAppointment) {
                    isInactive = "ACTIVE";
                }
            }

            // Set the patient's status based on the "isInactive" value
            patient.setStatus(isInactive);  // Set status as a String ("ACTIVE" or "INACTIVE")
        }

        // Save updated patients back to the database
        patientRepository.saveAll(patients);
    }

    // Scheduled task that runs every day at midnight (you can adjust the cron expression as needed)
    @Scheduled(cron = "0 0 0 * * ?")  // Runs daily at midnight
    public void scheduledDeactivation() {
        deactivateInactivePatients();  // Call the deactivateInactivePatients method to check and update patient status
    }
    

    public List<Patient> getPatientsWithNoShowAppointments() {
        // Find all appointments with status "Pending"
        List<Patient> pendingAppointments = appointmentRepository.findPatientsWithPastScheduledAppointments();

        // Extract and return distinct patients associated with pending appointments
        return pendingAppointments;
    }
            
}


    //@Scheduled(cron = "0 0 11 * * ?")  // Runs daily at 11 AM
//0 → seconds (run at the start of the minute)
//0 → minutes (run at the start of the hour)
//11 → hour (11 AM)
//* → day of the month (every day)
//* → month (every month)
//? → 
    
    


	








