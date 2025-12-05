package com.doan.bepsachviet_be.constant;

public enum OrderStatus {
  PENDING,      // Order created, waiting for payment (for MOMO) or admin processing
  CONFIRMED,    // Payment completed (for MOMO), ready to process
  SHIPPING,     // Order is being shipped
  DELIVERED,    // Order has been delivered
  CANCELED      // Order has been cancelled
}

