package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.constant.PaymentMethod;
import com.doan.bepsachviet_be.constant.PaymentStatus;
import com.doan.bepsachviet_be.entity.OrderEntity;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import com.doan.bepsachviet_be.repository.OrderRepository;
import com.doan.bepsachviet_be.service.EmailService;
import com.doan.bepsachviet_be.service.Impl.MomoService;
import com.doan.bepsachviet_be.service.OrderService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payment/momo")
@RequiredArgsConstructor
@Slf4j
public class MomoController {

  private final MomoService momoService;
  private final OrderService orderService;
  private final OrderRepository orderRepository;
  private final EmailService emailService;


  @PostMapping("/create")
  public ResponseEntity<?> createPayment(@RequestBody Map<String, Object> paymentRequest) {
    try {
      String orderId = (String) paymentRequest.get("orderId");

      if (orderId == null || orderId.isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "orderId is required"));
      }

      // Fetch real order from database
      OrderEntity order = orderRepository.findByOrderId(orderId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Order not found: " + orderId));

      // Validate payment method
      if (order.getPaymentMethod() != PaymentMethod.MOMO) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Order payment method is not MOMO"));
      }

      // Validate payment status
      if (order.getPaymentStatus() == PaymentStatus.COMPLETED) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Order already paid"));
      }

      // Create MoMo payment
      Map<String, Object> momoRes = momoService.createPayment(order);

      log.info("✅ MoMo payment created for order: {}", orderId);
      return ResponseEntity.ok(momoRes);

    } catch (ResponseStatusException e) {
      log.error("❌ Error creating MoMo payment: {}", e.getReason());
      return ResponseEntity.status(e.getStatusCode())
          .body(Map.of("error", e.getReason()));
    } catch (Exception e) {
      log.error("❌ Error creating MoMo payment: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to create payment: " + e.getMessage()));
    }
  }

  @GetMapping("/return")
  public ResponseEntity<?> returnPayment(@RequestParam Map<String, String> params) {
    try {
      String orderId = params.get("orderId");
      String resultCode = params.get("resultCode");

      log.info("✅ MoMo return callback - orderId: {}, resultCode: {}", orderId, resultCode);

      OrderResponse order = orderService.getOrderById(orderId);

      return ResponseEntity.ok(Map.of(
          "message", "Payment processed",
          "orderId", orderId,
          "resultCode", resultCode,
          "paymentStatus", order.getPaymentStatus()
      ));
    } catch (Exception e) {
      log.error("❌ Error processing return: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/ipn-handler")
  public ResponseEntity<?> handleIpn(@RequestBody Map<String, Object> body) {
    try {
      log.info("📥 Received MoMo IPN callback: {}", body);

      // Verify signature
      if (!momoService.verifySignature(body)) {
        log.error("❌ Invalid signature in IPN callback");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of("error", "Invalid signature"));
      }

      String orderId = (String) body.get("orderId");
      String resultCode = String.valueOf(body.get("resultCode"));
      String transactionId = String.valueOf(body.get("transId"));

      // Fetch order
      OrderEntity order = orderRepository.findByOrderId(orderId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
              "Order not found: " + orderId));

      // Store transaction ID
      order.setTransactionId(transactionId);

      if ("0".equals(resultCode)) {
        // Payment successful
        order.setPaymentStatus(PaymentStatus.COMPLETED);

        // Update order status from PENDING to CONFIRMED
        if (order.getStatus() == OrderStatus.PENDING) {
          order.setStatus(OrderStatus.CONFIRMED);
        }

        orderRepository.save(order);
        log.info("✅ Payment successful for order: {}, transactionId: {}", orderId, transactionId);

        // Send order confirmation email after successful payment
        try {
          emailService.sendOrderConfirmationEmail(order);
          log.info("📧 Order confirmation email sent for order: {}", orderId);
        } catch (Exception emailEx) {
          log.error("❌ Failed to send order confirmation email for order {}: {}", orderId, emailEx.getMessage());
        }

      } else {
        // Payment failed
        order.setPaymentStatus(PaymentStatus.FAILED);
        orderRepository.save(order);
        log.warn("❌ Payment failed for order: {}, resultCode: {}", orderId, resultCode);
      }

      return ResponseEntity.ok(Map.of(
          "message", "Received and processed",
          "resultCode", 0
      ));

    } catch (Exception e) {
      log.error("❌ Error processing IPN: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", e.getMessage()));
    }
  }
}
