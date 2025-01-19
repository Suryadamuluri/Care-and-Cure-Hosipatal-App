package com.hms.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hms.validation.ValidFutureDate;
import com.hms.validation.ValidLocalTime;

@Entity
public class Appointment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int appointmentId;

    @ManyToOne
    @JsonIgnoreProperties("appointmentList")
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctorObj;

    @ManyToOne
    @JsonIgnoreProperties("appointmentList")
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patientObj;

    @NotNull(message = "Appointment date cannot be empty")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    @ValidFutureDate(message = "Appointment date cannot be more than 1 month in the future.")
     @JsonFormat(pattern = "dd-MM-yyyy")  
     private LocalDate appointmentDate;
    
    @ValidLocalTime
    @NotNull(message = "Start time cannot be null")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;
     
    private LocalTime endTime;
    
    @NotBlank(message = "Reason cannot be empty")
    @Size(max = 255, message = "Reason for visit cannot be longer than 255 characters")
    private String reasonForVisit;

    private String doctorReport;

//    @Size(max = 255, message = "Suggested medicine cannot be longer than 255 characters")
    private String medicineSuggested;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("appointment")
    private Bill billObj;

    @Pattern(regexp = "Scheduled|Completed|Cancelled", message = "Status must be one of 'Scheduled', 'Completed', or 'Cancelled'")
    private String status;
    
    public Appointment() {
    }

    public Appointment(Doctor doctorObj, Patient patientObj, LocalDate appointmentDate, LocalTime startTime) {
        this.doctorObj = doctorObj;
        this.patientObj = patientObj;
        this.appointmentDate =appointmentDate;
        this.startTime = startTime;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
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

    public Bill getBillObj() {
        return billObj;
    }

    public void setBillObj(Bill billObj) {
        this.billObj = billObj;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String i) {
        this.status = i;
    }
   public boolean isBooked() {
        return this.startTime != null && this.endTime != null && this.appointmentDate != null;
    }

    public boolean isCancelledByPatient() {
      
        return false; 
    }
   
    public void calculateEndTime() {
        if (this.startTime != null) {
            this.endTime = this.startTime.plusHours(1); // Add 1 hour to the start time
        }
    }
}
