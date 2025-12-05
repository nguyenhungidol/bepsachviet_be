package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.entity.OrderEntity;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
    void sendOrderConfirmationEmail(OrderEntity order);
}

