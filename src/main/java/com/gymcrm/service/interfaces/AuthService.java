package com.gymcrm.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    UserDetails authenticate(String username, String password);
    UserDetails loadUserByUsername(String username);
    void changePassword(String username, String oldPassword, String newPassword);
}
