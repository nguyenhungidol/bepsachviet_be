package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.OrderEntity;
import com.doan.bepsachviet_be.entity.OrderItemEntity;
import com.doan.bepsachviet_be.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reset Your Password - Bep Sach Viet");

        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;
        String emailBody = "Hello,\n\n" +
                "You have requested to reset your password. Please click the link below to reset your password:\n\n" +
                resetUrl + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Bep Sach Viet Team";

        message.setText(emailBody);
        mailSender.send(message);
    }

    @Override
    public void sendOrderConfirmationEmail(OrderEntity order) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(order.getUser().getEmail());
        message.setSubject("Order Confirmation - Bep Sach Viet #" + order.getOrderId().substring(0, 8));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(order.getUser().getName()).append(",\n\n");
        emailBody.append("Thank you for your order! Your order has been successfully placed.\n\n");
        emailBody.append("═══════════════════════════════════════════════\n");
        emailBody.append("ORDER DETAILS\n");
        emailBody.append("═══════════════════════════════════════════════\n\n");
        emailBody.append("Order ID: ").append(order.getOrderId()).append("\n");
        emailBody.append("Order Date: ").append(order.getCreatedAt()).append("\n");
        emailBody.append("Status: ").append(order.getStatus()).append("\n");
        emailBody.append("Payment Method: ").append(order.getPaymentMethod()).append("\n");
        emailBody.append("Payment Status: ").append(order.getPaymentStatus()).append("\n\n");

        emailBody.append("───────────────────────────────────────────────\n");
        emailBody.append("DELIVERY INFORMATION\n");
        emailBody.append("───────────────────────────────────────────────\n\n");
        emailBody.append("Name: ").append(order.getDeliveryName()).append("\n");
        emailBody.append("Phone: ").append(order.getDeliveryPhone()).append("\n");
        emailBody.append("Address: ").append(order.getDeliveryAddress()).append("\n");
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            emailBody.append("Notes: ").append(order.getNotes()).append("\n");
        }
        emailBody.append("\n");

        emailBody.append("───────────────────────────────────────────────\n");
        emailBody.append("ORDER ITEMS\n");
        emailBody.append("───────────────────────────────────────────────\n\n");

        int itemNumber = 1;
        for (OrderItemEntity item : order.getOrderItems()) {
            emailBody.append(itemNumber++).append(". ").append(item.getProduct().getName()).append("\n");
            emailBody.append("   Quantity: ").append(item.getQuantity()).append("\n");
            emailBody.append("   Price: ").append(formatCurrency(item.getPrice())).append("\n");
            emailBody.append("   Subtotal: ").append(formatCurrency(item.getSubtotal())).append("\n\n");
        }

        emailBody.append("───────────────────────────────────────────────\n");
        emailBody.append("TOTAL AMOUNT: ").append(formatCurrency(order.getTotalAmount())).append("\n");
        emailBody.append("═══════════════════════════════════════════════\n\n");

        emailBody.append("You can track your order status by visiting:\n");
        emailBody.append(frontendUrl).append("/orders/").append(order.getOrderId()).append("\n\n");

        emailBody.append("If you have any questions, please contact our customer service.\n\n");
        emailBody.append("Thank you for shopping with us!\n\n");
        emailBody.append("Best regards,\n");
        emailBody.append("Bep Sach Viet Team\n");
        emailBody.append(frontendUrl);

        message.setText(emailBody.toString());

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't fail the order creation
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        return currencyFormat.format(amount) + " đ";
    }
}
