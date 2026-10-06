package com.aditya.solarmanagement.service;

import com.aditya.solarmanagement.dto.LoginRequest;
import com.aditya.solarmanagement.dto.LoginResponse;
import com.aditya.solarmanagement.dto.MessageResponse;
import com.aditya.solarmanagement.dto.PasswordChangeRequest;

public interface AuthService {
	LoginResponse login(LoginRequest request);
	MessageResponse changePassword(String emailAddress, PasswordChangeRequest request);
	MessageResponse passwordResetHelp();
}
