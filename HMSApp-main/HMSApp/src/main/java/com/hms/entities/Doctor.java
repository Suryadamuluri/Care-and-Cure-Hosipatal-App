package com.hms.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private int doctorId;

    @NotBlank(message = "Doctor name cannot be blank")
    @Size(min = 4, max = 30, message = "Doctor name must be between 4 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Doctor name must contain only alphabets and spaces")
    @Column
    private String doctorName;

    @NotBlank(message = "Specialization cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Specialization must contain only alphabets and spaces")
    @Column
    private String specialization;

    @NotBlank(message = "Qualification cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Qualification must contain only alphabets and spaces")
    @Column
    private String qualification;

    @NotBlank(message = "Contact number cannot be blank")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be a valid 10-digit number")
    @Column
    private String contactNumber;

    @NotBlank(message = "Email ID cannot be blank")
    @Email(message = "Email ID should be valid")
    @Column
    private String emailId;

    @NotBlank(message = "Gender cannot be blank")
    @Pattern(regexp = "^(Male|Female|Other)$", message = "Gender must be one of: Male, Female, or Other")
    @Column
    private String gender;

    @NotBlank(message = "Location cannot be blank")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Location must contain only alphabets and spaces")
    @Column
    private String location;

    @NotNull(message = "Consultation fees cannot be null")
    @Min(value = 0, message = "Consultation fees must be a positive number")
    @Column
    private double consultationFees;

    @NotNull(message = "Date of joining cannot be null")
    @Column
    private LocalDate dateOfJoining;

    @NotNull(message = "Surgeon status cannot be null")
    @Column
    private boolean Surgeon;

    @NotNull(message = "Years of experience cannot be null")
    @Min(value = 0, message = "Years of experience must be greater than or equal to 0")
    @Column
    private int yearsOfExperience;

    @Column
    private boolean status=true;

    
    @OneToOne
    @JoinColumn(name="user_name")
    private UserInfo user;
    
    
    public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

    // Getters and setters

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getConsultationFees() {
        return consultationFees;
    }

    public void setConsultationFees(double consultationFees) {
        this.consultationFees = consultationFees;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public boolean isSurgeon() {
        return Surgeon;
    }

    public void setSurgeon(boolean Surgeon) {
        this.Surgeon = Surgeon;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

	public void setId(int id) {
		// TODO Auto-generated method stub
		
	}
	public boolean isstatus() {
	    return status;
	}

	public void setstatus(boolean status) {
		this.status = status; 
		
	}

	
	}

