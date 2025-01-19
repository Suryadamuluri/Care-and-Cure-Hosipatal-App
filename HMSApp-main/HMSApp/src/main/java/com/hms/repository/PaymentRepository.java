package com.hms.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hms.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Example of a valid custom query method:

	    @Query("SELECT p FROM Payment p WHERE p.id = :id")
	    Payment findPaymentWithDetails(@Param("id") Long id);
	    
	    List<Payment> findByBillObj_Appointment_PatientObj_PatientId(int patientId);

	    List<Payment> findByBillObj_BillId(int billId);
	    public List<Payment> findByPaymentStatusAndAmountPaidBetween(String paymentStatus, Double minAmount, Double maxAmount);

	    public List<Payment> findByPaymentStatusAndPaymentDateBetween(String paymentStatus, LocalDate paymentDateFrom, LocalDate paymentDateTo);


	    // Filter by payment date range
	    public   List<Payment> findByPaymentDateBetween(LocalDate paymentDateFrom, LocalDate paymentDateTo);

	    // Filter by amount range
	    public  List<Payment> findByAmountPaidBetween(Double minAmount, Double maxAmount);

	    // Filter by payment status
	    public List<Payment> findByPaymentStatus(String paymentStatus);


	    // Filter by amount greater than or equal to a minimum amount
	    public List<Payment> findByAmountPaidGreaterThanEqual(Double minAmount);

	    // Filter by amount less than or equal to a maximum amount
	    public    List<Payment> findByAmountPaidLessThanEqual(Double maxAmount);

	    // Return all payments if no filters are applied
	    public  List<Payment> findAll();
	    Page<Payment> findAll(Pageable pageable);
	    @Query("SELECT p FROM Payment p WHERE " +
	       "(:paymentStatus IS NULL OR p.paymentStatus = :paymentStatus) AND " +
	       "(:paymentDateFrom IS NULL OR p.paymentDate >= :paymentDateFrom) AND " +
	       "(:paymentDateTo IS NULL OR p.paymentDate <= :paymentDateTo) AND " +
	       "(:minAmount IS NULL OR p.amountPaid >= :minAmount) AND " +
	       "(:maxAmount IS NULL OR p.amountPaid <= :maxAmount) AND " +
	       "(:paymentMethod IS NULL OR p.paymentMethod = :paymentMethod)")
	    Page<Payment> findFilteredPayments(String paymentStatus, LocalDate paymentDateFrom, LocalDate paymentDateTo,
	                                    Double minAmount, Double maxAmount, String paymentMethod, Pageable pageable);

}