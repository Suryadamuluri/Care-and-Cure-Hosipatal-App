package com.hms.exception;

import org.springframework.http.HttpStatus;

public class InvalidEntityException extends Exception {
	public InvalidEntityException(String msg) {
		super(msg);
	}

}
