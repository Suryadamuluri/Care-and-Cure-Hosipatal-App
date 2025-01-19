package com.hms.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hms.entities.Appointment;
import com.hms.entities.Doctor;
import com.hms.entities.Patient;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    public Appointment saveAppointment(Appointment appointment,int doctorId,int patientId) throws InvalidEntityException {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new InvalidEntityException("Doctor not found with the ID " + doctorId));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new InvalidEntityException("Patient not found with the ID " + patientId));
        appointment.setDoctorObj(doctor);
        appointment.setPatientObj(patient);
        appointment.setAppointmentDate(appointment.getAppointmentDate());
        appointment.setStartTime(appointment.getStartTime());
        appointment.setReasonForVisit(appointment.getReasonForVisit());

        appointment.calculateEndTime();

        // Check if there is any conflict with existing appointments
        if (hasAppointmentConflict(appointment)) {
            throw new InvalidEntityException("The doctor already has an appointment scheduled during the requested time.");
        }

        // Set the status to "Scheduled" and save the appointment to the database
        appointment.setStatus("Scheduled");
        return appointmentRepository.save(appointment);
    }


    public boolean hasAppointmentConflict(Appointment appointment) {
        List<Appointment> overlappingAppointments = appointmentRepository.findAppointmentsByDoctorAndTimeOverlap(
                appointment.getDoctorObj().getDoctorId(),  // Use appointment.getDoctor() instead of appointment.getDoctorObj()
                appointment.getAppointmentDate(),
                appointment.getStartTime(),
                appointment.getEndTime()
        );
        return !overlappingAppointments.isEmpty();
    }
    
    public String cancelAppointment(int appointmentId) throws InvalidEntityException{
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidEntityException("Appointment not found"));
        if ((appointment.getAppointmentDate().isAfter(LocalDate.now()) || appointment.getAppointmentDate().isEqual(LocalDate.now())) && appointment.getStatus().equalsIgnoreCase("Scheduled")) {
            appointment.setStatus("Cancelled");
            appointmentRepository.save(appointment);

            return "Appointment cancelled successfully.";
        } else {
            throw new InvalidEntityException( "Cannot cancel an appointment in the past / completed appointment.");
        }
    }

    public String rescheduleAppointment(int appointmentId, LocalDate newDate, LocalTime newTime) throws InvalidEntityException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidEntityException("Appointment not found"));

        
        if (appointment.getStatus().equalsIgnoreCase("Scheduled") && (appointment.getAppointmentDate().isAfter(LocalDate.now()) || appointment.getAppointmentDate().isEqual(LocalDate.now()))) {
            appointment.setAppointmentDate(newDate);
            appointment.setStartTime(newTime);
            appointment.calculateEndTime(); // Update the end time as well
            
            if (hasAppointmentConflict(appointment)) {
                throw new InvalidEntityException("The doctor already has an appointment scheduled during the requested time.");
            }

            
            appointmentRepository.save(appointment);
            return "Appointment rescheduled successfully.";
        } else {
        	throw new InvalidEntityException("Only scheduled appointments can be rescheduled.");
        }
    }
   
    public Appointment getAppointmentById( int apid) throws InvalidEntityException {
        return appointmentRepository.findById(apid)
                .orElseThrow(() -> new InvalidEntityException("Appointment with id " + apid + " not found"));
    }
    public void deleteAppointment(int id) {
        appointmentRepository.deleteById(id);
    }
    public List<Patient> getPatientsWithAppointmentsOnDate(LocalDate date) throws InvalidEntityException {
    	List<Patient>patients=appointmentRepository.findPatientsWithAppointmentsOnDate(date);
    	if(patients==null || patients.isEmpty())
        {
        	throw new InvalidEntityException("No Appointment for current day");
        }
        return patients;
    }
    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        List<Appointment> allAppointments = appointmentRepository.findAll();
        return allAppointments.stream()
                .filter(appointment -> appointment.getAppointmentDate().equals(date))
                .collect(Collectors.toList());
    }
    public void updateAppointmentDetails(int appointmentId, String doctorReport, String medicinesSuggested) throws InvalidEntityException {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new InvalidEntityException("Appointment not found for ID: " + appointmentId));

        // Check if the appointment status is "Scheduled"
        if (!"Scheduled".equalsIgnoreCase(appointment.getStatus())) {
            throw new InvalidEntityException("Cannot update an appointment with status: " + appointment.getStatus());
        }

        // Update only the specified fields
        appointment.setDoctorReport(doctorReport);
        appointment.setMedicineSuggested(medicinesSuggested);

        // Save updated appointment
        appointmentRepository.save(appointment);
    }
    
    
    
}