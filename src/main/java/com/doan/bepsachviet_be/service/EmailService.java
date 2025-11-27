package com.doan.bepsachviet_be.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
}

