package com.hms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hms.entities.UserInfo;
import com.hms.exception.InvalidEntityException;
import com.hms.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {


	@Autowired
	UserRepository userRepo;
	
    public UserInfo addUser(UserInfo user) throws InvalidEntityException {
    	if(userRepo.findByUserName(user.getUserName())!=null)
    		throw new InvalidEntityException("Username "+user.getUserName()+" already exists. Please provide a small change in name" );
    	
    	return userRepo.save(user);
    }

    public UserInfo authenticate(UserInfo user) throws InvalidEntityException {
        UserInfo result = userRepo.findByUserNameAndPassword(user.getUserName(),user.getPassword());
        if(result==null)
        	throw new InvalidEntityException("Invalid credentials");
        else
        	return result;
    }

    public List<UserInfo> getAllUsers() {
        return userRepo.findAll();
    }
}

