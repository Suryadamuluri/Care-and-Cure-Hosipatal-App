package com.hms.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserInfo {
	
	@Id
    private String userName;
    private String password;
    private String role;

    public UserInfo(String userName, String password, String role) {
        this.userName = userName;
        this.password = password;
        this.role = role;
    }
    public UserInfo() {
    	
    }
    
    // Getters and Setters
    
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}   
}

