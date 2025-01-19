package com.hms.entities;

import java.time.LocalDate;
import java.util.List;

public class Bill {
	private long billId;
	private Appointment appointment;
	private String description;
	private double consultationFees;	
	private double medicineFees;
	private double testCharge;
	private float discountPercentage;
	private double taxes;

	private LocalDate billDate;
	private List<Payment> pmtList;

	private double  miscellaneousCharge;

	private double totalAmount; // Persisted field

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

	public Bill(long billId, double consultationFees, double medicineFees, double testCharge, double miscellaneousCharge,
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



	public Bill() {}

	public long  getBillId() {
		return billId;
	}

	public void setBillId(long l) {
		this.billId = l;
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

	public void setStatus(String string) {
		// TODO Auto-generated method stub

	}

}

