package com.hms.controller;

import java.time.LocalDate;

public class PaymentFilter {
    private String paymentDate;
    private String paymentMethod;
    private String paymentStatus;
    private Double minAmount;
    private Double maxAmount;
   
        private LocalDate paymentDateFrom;
        private LocalDate paymentDateTo;
        
        public LocalDate getPaymentDateTo() {
			return paymentDateTo;
		}

		public void setPaymentDateTo(LocalDate paymentDateTo) {
			this.paymentDateTo = paymentDateTo;
		}

		// Getters and setters
        public LocalDate getPaymentDateFrom() {
            return paymentDateFrom;
        }

        public void setPaymentDateFrom(LocalDate paymentDateFrom) {
            this.paymentDateFrom = paymentDateFrom;
        }
    

    // Getters and setters
    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
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

    public Double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Double minAmount) {
        this.minAmount = minAmount;
    }

    public Double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Double maxAmount) {
        this.maxAmount = maxAmount;
    }
}
