package com.hms.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;



@Entity
@JsonIgnoreProperties("pmtList")
public class Bill {
	 @Id
	
	 @GeneratedValue(strategy = GenerationType.IDENTITY) 
	 @Column(name = "bill_id")
	 private int billId;
	 @OneToOne
	 @JoinColumn(name = "appointment_id", nullable = false)
	 @JsonIgnoreProperties("billObj")
	 private Appointment appointment;
	 @NotBlank(message = "Description cannot be blank")
	    @Size(max = 60, message = "Description must be less than or equal to 255 characters")
	    private String description;

	    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fees must be greater than 0")
	    private double consultationFees;

	    @DecimalMin(value = "0.0", inclusive = false, message = "Medicine fees must be greater than 0")
	    private double medicineFees;

	    @DecimalMin(value = "0.0", inclusive = true, message = "Test charge cannot be negative")
	    private double testCharge;

	    @DecimalMin(value = "0.0", inclusive = true, message = "Discount percentage cannot be negative")
	    @DecimalMax(value = "100.0", message = "Discount percentage cannot be more than 100")
	    private float discountPercentage;

	    @DecimalMin(value = "0.0", inclusive = true, message = "Taxes cannot be negative")
	    private double taxes;
	
	 private LocalDate billDate;
	  @OneToMany(mappedBy = "billObj", cascade = CascadeType.ALL, orphanRemoval = true)
	    private List<Payment> pmtList;
	   
	  @DecimalMin(value = "0.0", inclusive = true, message = "Miscellaneous charge cannot be negative")
	  private double  miscellaneousCharge;
		
	  private double totalAmount; // Persisted field

	    @Transient
	    private double totalPaid; // 
	   
	  
    
	public double getTotalAmount() {
			return totalAmount;
		}

		public void setTotalAmount(double totalAmount) {
			this.totalAmount = totalAmount;
		}

		public double getTotalPaid() {
			return totalPaid;
		}

		public void setTotalPaid(double totalPaid) {
			this.totalPaid = totalPaid;
		}

	public LocalDate getBillDate() {
		return billDate;
	}

	public void setBillDate(LocalDate billDate) {
		this.billDate = billDate;
	}

	public double getTestCharge() {
		return testCharge;
	}
	 
	 public Bill(int billId, double consultationFees, double medicineFees, double testCharge, double miscellaneousCharge,
             String description, float discountPercentage, double taxes, LocalDate billDate, 
             Appointment appointment, List<Payment> pmtList, double totalAmount, double totalPaid) {
     this.billId = billId;
     this.consultationFees = consultationFees;
     this.medicineFees = medicineFees;
     this.testCharge = testCharge;
     this.miscellaneousCharge = miscellaneousCharge;
     this.description = description;
     this.discountPercentage = discountPercentage;
     this.taxes = taxes;
     this.billDate = billDate;
     this.appointment = appointment;
     this.pmtList = pmtList;
     this.totalAmount = totalAmount;
     this.totalPaid = totalPaid;
 }
   
	 public void setStatus(String string) {
			// TODO Auto-generated method stub
			
		}

		public String getStatus() {
			// TODO Auto-generated method stub
			return null;
		}
	   
    
    public Bill() {}

	public int  getBillId() {
		return billId;
	}

	public void setBillId(int billId) {
		this.billId = billId;
	}

	public double getConsultationFees() {
		return consultationFees;
	}

	public void setConsultationFees(double consultationFees) {
		this.consultationFees = consultationFees;
	}

	public double getMedicineFees() {
		return medicineFees;
	}

	public void setMedicineFees(double medicineFees) {
		this.medicineFees = medicineFees;
	}

	
	public void setTestCharge(double testCharge) {
		this.testCharge = testCharge;
	}

	public double getMiscellaneousCharge() {
		return miscellaneousCharge;
	}

	public void setMiscellaneousCharge(double miscellaneouscharge) {
		this.miscellaneousCharge = miscellaneouscharge;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	
	


	public float getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(float discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public double getTaxes() {
		return taxes;
	}

	public void setTaxes(double taxes) {
		this.taxes = taxes;
	}

	

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
	}

	public List<Payment> getPmtList() {
		return pmtList;
	}

	public void setPmtList(List<Payment> pmtList) {
		this.pmtList = pmtList;
	}
   
}

    