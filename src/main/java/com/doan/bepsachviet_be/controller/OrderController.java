package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.io.Request.CreateOrderRequest;
import com.doan.bepsachviet_be.io.Request.UpdateOrderStatusRequest;
import com.doan.bepsachviet_be.io.Response.OrderResponse;
import com.doan.bepsachviet_be.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  // User endpoints
  @PostMapping("/orders")
  @ResponseStatus(HttpStatus.CREATED)
  public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request,
                                    Authentication authentication) {
    String userId = authentication.getName();
    return orderService.createOrder(request, userId);
  }

  @GetMapping("/orders/my-orders")
  public Page<OrderResponse> getMyOrders(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          Authentication authentication) {
    String userId = authentication.getName();
    Pageable pageable = PageRequest.of(page, size);
    return orderService.getUserOrders(userId, pageable);
  }

  @GetMapping("/orders/{orderId}")
  public OrderResponse getOrderById(@PathVariable String orderId) {
    return orderService.getOrderById(orderId);
  }

  // Admin endpoints
  @GetMapping("/admin/orders")
  public Page<OrderResponse> getAllOrders(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) OrderStatus status) {
    Pageable pageable = PageRequest.of(page, size);

    if (status != null) {
      return orderService.getOrdersByStatus(status, pageable);
    }

    return orderService.getAllOrders(pageable);
  }

  @GetMapping("/admin/orders/{orderId}")
  public OrderResponse getOrderByIdAdmin(@PathVariable String orderId) {
    return orderService.getOrderById(orderId);
  }

  @PatchMapping("/admin/orders/{orderId}/status")
  public OrderResponse updateOrderStatus(@PathVariable String orderId,
                                         @Valid @RequestBody UpdateOrderStatusRequest request) {
    return orderService.updateOrderStatus(orderId, request.getStatus());
  }
}

