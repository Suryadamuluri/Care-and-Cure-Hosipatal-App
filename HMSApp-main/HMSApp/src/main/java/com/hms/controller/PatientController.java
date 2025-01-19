
package com.hms.controller;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hms.entities.Patient;
import com.hms.exception.InvalidEntityException;
import com.hms.service.PatientService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService service;

    // Add a new patient with validation and error handling
    @PostMapping("/addPatient")
    public ResponseEntity<?> addPatient(@Valid @RequestBody Patient patient) throws InvalidEntityException {
       

         
            Patient savedPatient = service.addPatient(patient);
            return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);
    } 
    @GetMapping("/fetchByUserName/{userName}")
    public ResponseEntity<Patient> viewPatientByUserName(@PathVariable("userName") String userName) {
        return new ResponseEntity<>(service.getPatientByUserName(userName), HttpStatus.OK);
    }


    // View all patients
    @GetMapping("/viewAllPatients")
    public ResponseEntity<List<Patient>> viewAllPatients() {
        try {
            List<Patient> patients = service.viewAllPatients();
            if (patients.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(patients);
        } catch (Exception ex) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // View a patient by ID with error handling
    @GetMapping("/viewPatientById/{id}")
    public ResponseEntity<?> viewPatientById(@PathVariable("id") int id) {
        try {
            Patient patient = service.getPatientById(id);
            if (patient == null) {
                return new ResponseEntity<>("Patient not found", HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(patient);
        } catch (Exception ex) {
            return new ResponseEntity<>("Error while retrieving patient: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/viewPatientByName/{name}")
    public ResponseEntity<?> getPatientByName(@PathVariable("name") String name) {
        List<Patient> patients = service.getPatientByName(name.trim());  // Trim to remove leading/trailing spaces

        // Log the list of patients for debugging
        System.out.println("Found patients: " + patients);

        if (patients.isEmpty()) {
            return new ResponseEntity<>("Patient with the name " + name + " not found", HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(patients);
    }
    @GetMapping("/viewPatientByMedicalHistory/{history}")
    public ResponseEntity<?> getPatientByMedicalHistory(@PathVariable("history") String medicalHistory) {
        List<Patient> patients = service.getPatientsByMedicalHistory(medicalHistory.trim());  // Trim to remove leading/trailing spaces

        // Log the list of patients for debugging
        System.out.println("Found patients with medical history: " + medicalHistory);

        if (patients.isEmpty()) {
            return new ResponseEntity<>("No patients found with the medical history: " + medicalHistory, HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok(patients);
    }

   
    
   
   
    @PutMapping("/updatePatient/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable("id") int id, @RequestBody Patient updatedPatient) {
        // Handle validation errors
    	
       
            // Fetch the existing patient by ID
            Patient existingPatient = service.getPatientById(id);
            
            if (existingPatient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Patient not found."); // Patient not found
            }

            // Ensure the ID is preserved in the updated patient object
            updatedPatient.setPatientId(id); 
            updatedPatient.setUser(existingPatient.getUser());
            updatedPatient.setDateOfBirth(existingPatient.getDateOfBirth());
            updatedPatient.setStatus(existingPatient.getStatus());
            // Call service to update the patient in the database
            Patient savedPatient = service.updatePatient(updatedPatient);

            return ResponseEntity.ok(savedPatient); // Return the updated patient in the response
        
    }



    @GetMapping("/by-doctor-and-date")
    public ResponseEntity<?> getPatientsByDoctorAndDate(
            @RequestParam int doctorId,
            @RequestParam @DateTimeFormat LocalDate appDate) throws InvalidEntityException {
        List<Patient> patients = service.getPatientsByDoctorAndDate(doctorId, appDate);
        
        if (patients == null || patients.isEmpty()) {
            throw new InvalidEntityException("No patients found for doctorId: " + doctorId + " on date: " + appDate);
        }
        return ResponseEntity.ok(patients);
    }

    @PutMapping("/deactivate-inactive")
    public ResponseEntity<String> deactivateInactivePatients() {
        service.deactivateInactivePatients();
        return ResponseEntity.ok("Inactive patients have been deactivated successfully.");
    }
    @GetMapping("/no-show-report")
    public ResponseEntity<List<Patient>> getNoShowReport() {
        List<Patient> noShowPatients = service.getPatientsWithNoShowAppointments();
        System.out.println("Returning no-show patients: " + noShowPatients);
        return ResponseEntity.ok(noShowPatients);
    }

        
    }


    