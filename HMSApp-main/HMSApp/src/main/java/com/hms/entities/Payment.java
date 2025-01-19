package com.hms.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Payment {

    @Id
    @Column(name = "payment_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long paymentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bill_id", nullable = false)
    @JsonIgnore
    private Bill billObj;

    @NotNull(message = "Transaction ID cannot be null")
    @Size(min = 10, max = 50, message = "Transaction ID must be between 10 and 50 characters")
    private String transactionId;

    @NotNull(message = "Payment date cannot be null")
    @Temporal(TemporalType.DATE)  // Ensures that only the date part is stored, not the time
    @DateTimeFormat(pattern = "yyyy-MM-dd") // Bind the date format to yyyy-MM-dd
    @PastOrPresent(message = "Payment date cannot be in the future")
    private LocalDate paymentDate;

    @Positive(message = "Amount paid must be greater than zero")
    private double amountPaid;

    @NotNull(message = "Payment method cannot be null")
    @Pattern(regexp = "Cash|Credit Card|Debit Card|Net Banking|UPI", message = "Payment method must be one of the following: Cash, Credit Card, Debit Card, Net Banking, or UPI")
    private String paymentMethod;


    @NotNull(message = "Payment status cannot be null")
    @Pattern(regexp = "Paid|Unpaid|Partially Paid", message = "Payment status must be one of the following: Completed, Unpaid, or Partially Paid")
    @Column(name = "payment_status")
    private String paymentStatus;


    // Default constructor required by JPA
    public Payment() {}

    // Getters and Setters
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(long  paymentId) {
        this.paymentId = paymentId;
    }

    public Bill getBillObj() {
        return billObj;
    }

    public void setBillObj(Bill billObj) {
        this.billObj = billObj;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate localDate) {
        this.paymentDate = localDate;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
