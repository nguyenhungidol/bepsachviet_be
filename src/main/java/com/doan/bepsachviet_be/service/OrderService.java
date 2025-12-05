package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.constant.PaymentStatus;
import com.doan.bepsachviet_be.io.Request.CreateOrderRequest;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

  OrderResponse createOrder(CreateOrderRequest request, String userId);

  OrderResponse getOrderById(String orderId);

  Page<OrderResponse> getAllOrders(Pageable pageable);

  Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable);

  OrderResponse updateOrderStatus(String orderId, OrderStatus status);

  Page<OrderResponse> getUserOrders(String userId, Pageable pageable);

//  void updatePaymentStatus(String orderId, PaymentStatus paymentStatus);
}

