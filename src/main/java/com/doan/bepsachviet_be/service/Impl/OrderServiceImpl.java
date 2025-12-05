package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.constant.PaymentStatus;
import com.doan.bepsachviet_be.entity.OrderEntity;
import com.doan.bepsachviet_be.entity.OrderItemEntity;
import com.doan.bepsachviet_be.entity.ProductEntity;
import com.doan.bepsachviet_be.entity.UserEntity;
import com.doan.bepsachviet_be.io.Request.CreateOrderRequest;
import com.doan.bepsachviet_be.io.Request.OrderItemRequest;
import com.doan.bepsachviet_be.io.Response.OrderItemResponse;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import com.doan.bepsachviet_be.repository.OrderRepository;
import com.doan.bepsachviet_be.repository.ProductRepository;
import com.doan.bepsachviet_be.repository.UserRepository;
import com.doan.bepsachviet_be.service.EmailService;
import com.doan.bepsachviet_be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;
  private final EmailService emailService;

  @Override
  @Transactional
  public OrderResponse createOrder(CreateOrderRequest request, String email) {
    // Find user
    UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    // Validate stock availability for all items first
    for (OrderItemRequest itemRequest : request.getItems()) {
      ProductEntity product = productRepository.findByProductId(itemRequest.getProductId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Product not found: " + itemRequest.getProductId()));

      if (product.getStockQuantity() == null || product.getStockQuantity() < itemRequest.getQuantity()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Insufficient stock for product: " + product.getName() +
            ". Available: " + (product.getStockQuantity() != null ? product.getStockQuantity() : 0) +
            ", requested: " + itemRequest.getQuantity());
      }
    }

    // Create order
    OrderEntity order = OrderEntity.builder()
        .orderId(UUID.randomUUID().toString())
        .user(user)
        .status(OrderStatus.PENDING)
        .deliveryName(request.getDeliveryName())
        .deliveryPhone(request.getDeliveryPhone())
        .deliveryAddress(request.getDeliveryAddress())
        .notes(request.getNotes())
        .paymentMethod(request.getPaymentMethod())
        .totalAmount(BigDecimal.ZERO)
        .orderItems(new ArrayList<>())
        .build();

    // Create order items, calculate total, and decrement stock
    BigDecimal totalAmount = BigDecimal.ZERO;
    for (OrderItemRequest itemRequest : request.getItems()) {
      ProductEntity product = productRepository.findByProductId(itemRequest.getProductId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
              "Product not found: " + itemRequest.getProductId()));

      BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
      totalAmount = totalAmount.add(subtotal);

      // Decrement stock quantity
      product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
      productRepository.save(product);

      OrderItemEntity orderItem = OrderItemEntity.builder()
          .order(order)
          .product(product)
          .quantity(itemRequest.getQuantity())
          .price(product.getPrice())
          .subtotal(subtotal)
          .build();

      order.getOrderItems().add(orderItem);
    }

    order.setTotalAmount(totalAmount);
    OrderEntity savedOrder = orderRepository.save(order);

    // Email sending strategy:
    // - MOMO: Email sent after successful payment in IPN handler
    // - CASH_ON_DELIVERY: Email sent after admin confirms the order (status changed to CONFIRMED)
    // No automatic email is sent during order creation

    return convertToResponse(savedOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public OrderResponse getOrderById(String orderId) {
    OrderEntity order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    return convertToResponse(order);
  }

//  @Override
//  public Page<OrderResponse> getOrdersByUserId(String userId, Pageable pageable) {
//    Page<OrderEntity> orders = orderRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
//    return orders.map(this::convertToResponse);
//  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getAllOrders(Pageable pageable) {
    Page<OrderEntity> orders = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    return orders.map(this::convertToResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
    Page<OrderEntity> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    return orders.map(this::convertToResponse);
  }

  @Override
  @Transactional
  public OrderResponse updateOrderStatus(String orderId, OrderStatus status) {
    OrderEntity order = orderRepository.findByOrderId(orderId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

    OrderStatus oldStatus = order.getStatus();
    order.setStatus(status);

    // Auto-update payment status for CASH_ON_DELIVERY when delivered
    if (status == OrderStatus.DELIVERED &&
        order.getPaymentMethod() == com.doan.bepsachviet_be.constant.PaymentMethod.CASH_ON_DELIVERY &&
        order.getPaymentStatus() == PaymentStatus.PENDING) {
      order.setPaymentStatus(PaymentStatus.COMPLETED);
    }

    // Restore stock if order is cancelled and was previously in PENDING, CONFIRMED, or SHIPPING status
    if (status == OrderStatus.CANCELED &&
        (oldStatus == OrderStatus.PENDING || oldStatus == OrderStatus.CONFIRMED || oldStatus == OrderStatus.SHIPPING)) {
      for (OrderItemEntity orderItem : order.getOrderItems()) {
        ProductEntity product = orderItem.getProduct();
        product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
        productRepository.save(product);
      }
    }

    OrderEntity updatedOrder = orderRepository.save(order);

    // Send order confirmation email when admin confirms COD order
    if (status == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING &&
        order.getPaymentMethod() == com.doan.bepsachviet_be.constant.PaymentMethod.CASH_ON_DELIVERY) {
      try {
        emailService.sendOrderConfirmationEmail(updatedOrder);
        System.out.println("✅ Order confirmation email sent for COD order: " + orderId);
      } catch (Exception e) {
        System.err.println("❌ Failed to send order confirmation email for COD order " + orderId + ": " + e.getMessage());
      }
    }

    return convertToResponse(updatedOrder);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<OrderResponse> getUserOrders(String email, Pageable pageable) {
    Page<OrderEntity> orders = orderRepository.findByUser_EmailOrderByCreatedAtDesc(email, pageable);
    return orders.map(this::convertToResponse);
  }

  private OrderResponse convertToResponse(OrderEntity order) {
    List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
        .map(this::convertToOrderItemResponse)
        .collect(Collectors.toList());

    return OrderResponse.builder()
        .id(order.getId())
        .orderId(order.getOrderId())
        .userId(order.getUser().getUserId())
        .userName(order.getUser().getName())
        .userEmail(order.getUser().getEmail())
        .deliveryName(order.getDeliveryName())
        .deliveryPhone(order.getDeliveryPhone())
        .deliveryAddress(order.getDeliveryAddress())
        .notes(order.getNotes())
        .status(order.getStatus())
        .paymentMethod(order.getPaymentMethod())
        .paymentStatus(order.getPaymentStatus())
        .transactionId(order.getTransactionId())
        .totalAmount(order.getTotalAmount())
        .orderItems(itemResponses)
        .createdAt(order.getCreatedAt())
        .updatedAt(order.getUpdatedAt())
        .build();
  }

  private OrderItemResponse convertToOrderItemResponse(OrderItemEntity item) {
    return OrderItemResponse.builder()
        .id(item.getId())
        .productId(item.getProduct().getProductId())
        .productName(item.getProduct().getName())
        .productImage(item.getProduct().getImageSrc())
        .quantity(item.getQuantity())
        .price(item.getPrice())
        .subtotal(item.getSubtotal())
        .build();
  }

//  @Override
//  @Transactional
//  public void updatePaymentStatus(String orderId, PaymentStatus paymentStatus) {
//    OrderEntity order = orderRepository.findByOrderId(orderId)
//        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
//
//    order.setPaymentStatus(paymentStatus);
//    orderRepository.save(order);
//  }
}

