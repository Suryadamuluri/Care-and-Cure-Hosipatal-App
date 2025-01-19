package com.hms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.entities.UserInfo;

public interface UserRepository extends JpaRepository<UserInfo, String>  {

	//To authenticate user
	UserInfo findByUserNameAndPassword(String userName, String password);
	
	//To fetch the user based on userName
	UserInfo findByUserName(String userName);
}
