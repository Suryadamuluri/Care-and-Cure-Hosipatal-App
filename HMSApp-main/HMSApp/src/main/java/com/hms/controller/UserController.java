package com.hms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hms.entities.UserInfo;
import com.hms.exception.InvalidEntityException;
import com.hms.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserInfo register(@RequestBody UserInfo user) throws InvalidEntityException {
    	
    	return userService.addUser(user);
        
    }

    @PostMapping("/login")
    public UserInfo login(@RequestBody UserInfo user) throws InvalidEntityException {
        return userService.authenticate(user);
    }

    @GetMapping
    public List<UserInfo> getUsers() {
        return userService.getAllUsers();
    }
}

