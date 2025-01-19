package com.hms.entities;
import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Appointment {

    private int appointmentId; 
    private Doctor doctorObj;  
    private Patient patientObj;
    
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reasonForVisit;
    private String doctorReport;
    private String medicineSuggested;
    private String status; // "Scheduled", "Completed", or "Cancelled"

    // Default constructor
    public Appointment() {}

    // Parameterized constructor
    public Appointment(Integer appointmentId, Doctor doctorObj, Patient patientObj, LocalDate appointmentDate, 
                        LocalTime startTime, LocalTime endTime, String reasonForVisit, 
                        String doctorReport, String medicineSuggested, String status) {
        this.appointmentId = appointmentId;
        this.doctorObj = doctorObj;
        this.patientObj = patientObj;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reasonForVisit = reasonForVisit;
        this.doctorReport = doctorReport;
        this.medicineSuggested = medicineSuggested;
        this.status = status;
    }

    // Getters and setters
    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Doctor getDoctorObj() {
        return doctorObj;
    }

    public void setDoctorObj(Doctor doctorObj) {
        this.doctorObj = doctorObj;
    }

    public Patient getPatientObj() {
        return patientObj;
    }

    public void setPatientObj(Patient patientObj) {
        this.patientObj = patientObj;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getDoctorReport() {
        return doctorReport;
    }

    public void setDoctorReport(String doctorReport) {
        this.doctorReport = doctorReport;
    }

    public String getMedicineSuggested() {
        return medicineSuggested;
    }

    public void setMedicineSuggested(String medicineSuggested) {
        this.medicineSuggested = medicineSuggested;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
