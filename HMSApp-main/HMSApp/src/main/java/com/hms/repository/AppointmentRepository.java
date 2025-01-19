package com.hms.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hms.entities.Appointment;
import com.hms.entities.Patient;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {
	

	@Query("SELECT a FROM Appointment a WHERE a.doctorObj.doctorId = :doctorId AND a.appointmentDate = :appointmentDate AND (a.startTime <= :endTime AND a.endTime >= :startTime)")
	List<Appointment> findAppointmentsByDoctorAndTimeOverlap(int doctorId, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime);
	@Query("SELECT a.patientObj FROM Appointment a WHERE a.appointmentDate = :date")
	List<Patient> findPatientsWithAppointmentsOnDate(LocalDate date);
	List<Appointment> findByDoctorObj_DoctorId(Integer doctorId);
	//List<Appointment> findByStatus(String string);
	 @Query("SELECT a.patientObj FROM Appointment a WHERE a.doctorObj.doctorId = :doctorId AND a.appointmentDate = :appDate AND a.status!='Cancelled'")
	 List<Patient> findPatientsByDoctorAndDate(@Param("doctorId") int doctorId, @Param("appDate") LocalDate appDate);
	
	 @Query("SELECT a.patientObj FROM Appointment a WHERE a.appointmentDate < CURRENT_DATE " +
	           "AND a.status = 'Scheduled'")
	    List<Patient> findPatientsWithPastScheduledAppointments();
}