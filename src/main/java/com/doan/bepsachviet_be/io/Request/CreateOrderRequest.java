package com.doan.bepsachviet_be.io.Request;

import com.doan.bepsachviet_be.constant.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

  @NotBlank(message = "Delivery name is required")
  private String deliveryName;

  @NotBlank(message = "Delivery phone is required")
  private String deliveryPhone;

  @NotBlank(message = "Delivery address is required")
  private String deliveryAddress;

  private String notes;

  @NotNull(message = "Payment method is required")
  private PaymentMethod paymentMethod;

  @NotEmpty(message = "Order items cannot be empty")
  @Valid
  private List<OrderItemRequest> items;
}

