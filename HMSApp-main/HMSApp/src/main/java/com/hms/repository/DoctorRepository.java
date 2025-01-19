package com.hms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hms.entities.Appointment;
import com.hms.entities.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

	void deleteById(int id);

	//Optional<Doctor> findById(int id);

	//boolean existsById(int id);
	
	List<Doctor> findBySpecialization(String specialization);

	@Query("SELECT a FROM Appointment a WHERE a.doctorObj.id = :doctorId AND a.appointmentDate = :appointmentDate")
    List<Appointment> findAppointmentsByDoctorAndDate(@Param("doctorId") int doctorId, 
                                                      @Param("appointmentDate") LocalDate appointmentDate);

	List<Doctor> findBySpecializationAndStatusTrue(String specialization);
	
	Doctor findByUserUserName(String userName);
	
	
}

