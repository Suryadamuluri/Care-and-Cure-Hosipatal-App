package com.hms.service;

import com.hms.entities.Appointment;
import com.hms.entities.Doctor;
import com.hms.entities.UserInfo;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    UserService userService;

    // Add a new doctor
    public Doctor addDoctor(Doctor doctor) throws InvalidEntityException {
    	
    	String temp = doctor.getContactNumber().substring(6);//doctor.getContactNumber().length()-4);
        UserInfo u = new UserInfo(doctor.getDoctorName().substring(0,4)+temp, "test", "doctor");
        doctor.setUser(u);
        userService.addUser(u);        
    	
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // Send email notification
        sendEmailNotification(savedDoctor, "added");
        return savedDoctor;
    }

    // Get all doctors
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    // Get a doctor by ID
    public Doctor getDoctorById(int id) throws InvalidEntityException {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Doctor not found with ID: " + id));
    }

    // Update a doctor
    public Doctor updateDoctor(int id, Doctor updatedDoctor) throws InvalidEntityException {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new InvalidEntityException("Doctor not found with ID: " + id));

        // Update fields
        existingDoctor.setDoctorName(updatedDoctor.getDoctorName());
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization());
        existingDoctor.setQualification(updatedDoctor.getQualification());
        existingDoctor.setContactNumber(updatedDoctor.getContactNumber());
        existingDoctor.setEmailId(updatedDoctor.getEmailId());
        existingDoctor.setGender(updatedDoctor.getGender());
        existingDoctor.setLocation(updatedDoctor.getLocation());
        existingDoctor.setConsultationFees(updatedDoctor.getConsultationFees());
        existingDoctor.setSurgeon(updatedDoctor.isSurgeon());
        existingDoctor.setYearsOfExperience(updatedDoctor.getYearsOfExperience());

        Doctor savedDoctor = doctorRepository.save(existingDoctor);
        
        // Send email notification
        sendEmailNotification(savedDoctor, "updated");
        return savedDoctor;
    }

    // Delete a doctor
 // Delete a doctor
    public boolean deactivateDoctor(int id) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        if (optionalDoctor.isPresent()) {
            Doctor doctor = optionalDoctor.get();
            doctor.setstatus(false); // Set status to false
            doctorRepository.save(doctor); // Save updated doctor
            return true;
        }
        else
        	return false;
		
    }


    // Get doctors by specialization
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        // Fetch only active doctors with the given specialization
        return doctorRepository.findBySpecializationAndStatusTrue(specialization);
    }

    // Send email notification
    private void sendEmailNotification(Doctor doctor, String action) {
        String subject = "Doctor Record " + action;
        String emailContent = String.format(
        	    "Dear Admin,\n\n" +
        	    "We would like to inform you that Dr. %s has been %s in our system. Below are the details:\n\n" +
        	    "- Doctor ID: %s\n" +
        	    "- Specialization: %s\n" +
        	    "- Qualification: %s\n" +
        	    "- Contact: %s\n" +
        	    "- Email: %s\n" +
        	    "- Location: %s\n\n" +
        	    "Regards,\nYour Health Management System",
        	    doctor.getDoctorName(), // Doctor's name
        	    action,                 // Action (e.g., "added", "updated")
        	    doctor.getDoctorId(),   // Doctor ID
        	    doctor.getSpecialization(), // Specialization
        	    doctor.getQualification(),  // Qualification
        	    doctor.getContactNumber(),  // Contact Number
        	    doctor.getEmailId(),        // Email
        	    doctor.getLocation()        // Location
        	);
        if(action.equals("added")) {
        	emailContent += "Your user name is "+doctor.getUser().getUserName()+" and password is "+doctor.getUser().getPassword()+".";
        }


        try {
            emailService.sendEmail(doctor.getEmailId(), subject, emailContent); // Replace with actual admin email
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public List<String> getDoctorFreeSlots(int doctorId, LocalDate appointmentDate, LocalTime workingStartTime, LocalTime workingEndTime) {
        //List<Appointment> appointments = appointmentRepository.findAppointmentsByDoctorAndDate(doctorId, appointmentDate);
        List<Appointment> appointments = doctorRepository.findAppointmentsByDoctorAndDate(doctorId, appointmentDate);
        // Sort appointments by start time
        appointments.sort((a1, a2) -> a1.getStartTime().compareTo(a2.getStartTime()));

        List<String> freeSlots = new ArrayList<>();

        // Check for free time before the first appointment
        if (!appointments.isEmpty() && workingStartTime.isBefore(appointments.get(0).getStartTime())) {
            freeSlots.add(workingStartTime + " to " + appointments.get(0).getStartTime());
        }

        // Check between appointments
        for (int i = 0; i < appointments.size() - 1; i++) {
            LocalTime endCurrent = appointments.get(i).getEndTime();
            LocalTime startNext = appointments.get(i + 1).getStartTime();
            if (endCurrent.isBefore(startNext)) {
                freeSlots.add(endCurrent + " to " + startNext);
            }
        }

        // Check for free time after the last appointment
        if (!appointments.isEmpty() && workingEndTime.isAfter(appointments.get(appointments.size() - 1).getEndTime())) {
            freeSlots.add(appointments.get(appointments.size() - 1).getEndTime() + " to " + workingEndTime);
        }

        // If no appointments, doctor is free the entire working hours
        if (appointments.isEmpty()) {
            freeSlots.add(workingStartTime + " to " + workingEndTime);
        }
        for(String s: freeSlots)
        	System.out.println(s);

        return freeSlots;
    }
    
    public Doctor getDoctorByUserName(String username) {
    	return doctorRepository.findByUserUserName(username);
    }

}
