package com.hms.controller;

import com.hms.entities.Doctor;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.DoctorRepository;
import com.hms.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // Add a new doctor
    @PostMapping("/addDoctor")
    public ResponseEntity<?> addDoctor(@Valid @RequestBody Doctor doctor) throws InvalidEntityException {
            
        doctorService.addDoctor(doctor);
        return new ResponseEntity<>("Doctor added successfully!", HttpStatus.CREATED);
    }
    @GetMapping("/fetchByUserName/{userName}")
    public ResponseEntity<Doctor> viewDoctorByUserName(@PathVariable("userName") String userName) {
    	return new ResponseEntity<>(doctorService.getDoctorByUserName(userName),HttpStatus.OK);
    }

    // Get all doctors
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllDoctors() {
       
            List<Doctor> doctors = doctorService.getAllDoctors();
            return new ResponseEntity<>(doctors, HttpStatus.OK);
       
    }

    // Get a doctor by ID
    @GetMapping("/getDoctor/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable int id) throws InvalidEntityException {
      
            Doctor doctor = doctorService.getDoctorById(id);
            if (doctor == null) {
                return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(doctor, HttpStatus.OK);
        }
   

    @PutMapping("/updateDoctor/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable int id, @RequestBody Doctor doctor) throws InvalidEntityException {
       
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctor);
            if (updatedDoctor == null) {
                return new ResponseEntity<>("Doctor not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>("Doctor updated successfully!", HttpStatus.OK);
        } 
    
        @PutMapping("/leaveDoctor/{id}")
        public ResponseEntity<String> deactivateDoctor(@PathVariable int id) throws InvalidEntityException{
            
                boolean isDeactivated = doctorService.deactivateDoctor(id);
                if (isDeactivated) {
                    return ResponseEntity.ok("Doctor successfully deactivated.");
                } else {
                    throw new InvalidEntityException("Doctor id is invalid.");
                }
           
            }
 
 
 // Get doctors by specialization
        @GetMapping("/getBySpecialization/{specialization}")
        public ResponseEntity<?> getDoctorsBySpecialization(@PathVariable String specialization) {
            try {
                // Fetch only active doctors by specialization
                List<Doctor> activeDoctors = doctorService.getDoctorsBySpecialization(specialization);
                if (activeDoctors.isEmpty()) {
                    return new ResponseEntity<>("No active doctors found for this specialization", HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(activeDoctors, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>("Error while fetching doctors: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } 
    @GetMapping("/free-slots")
    public ResponseEntity<List<String>> getFreeSlots(
            @RequestParam int doctorId,
            @RequestParam String date,
            @RequestParam String clinicStartTime,
            @RequestParam String clinicEndTime) {

        LocalDate appointmentDate = LocalDate.parse(date);
        LocalTime start = LocalTime.parse(clinicStartTime.trim());
        LocalTime end = LocalTime.parse(clinicEndTime.trim());
        

        List<String> freeSlots = doctorService.getDoctorFreeSlots(doctorId, appointmentDate, start, end);
        return ResponseEntity.ok(freeSlots);
    }
    
}
