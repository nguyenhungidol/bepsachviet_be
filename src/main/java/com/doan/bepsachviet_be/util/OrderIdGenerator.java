package com.doan.bepsachviet_be.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class OrderIdGenerator {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

  /**
   * Generates an order ID in the format: ORD-YYMMDD-NNNN
   * Example: ORD-251207-0199
   *
   * @param orderNumber The sequential order number for the day (1-based)
   * @return The formatted order ID
   */
  public String generateOrderId(int orderNumber) {
    LocalDate today = LocalDate.now();
    String dateStr = today.format(DATE_FORMATTER);
    String numberStr = String.format("%04d", orderNumber);
    return String.format("ORD-%s-%s", dateStr, numberStr);
  }

  /**
   * Extracts the date portion from an order ID
   *
   * @param orderId The order ID in format ORD-YYMMDD-NNNN
   * @return The date string in YYMMDD format, or null if invalid
   */
  public String extractDateFromOrderId(String orderId) {
    if (orderId != null && orderId.matches("ORD-\\d{6}-\\d{4}")) {
      return orderId.substring(4, 10);
    }
    return null;
  }

  /**
   * Extracts the order number from an order ID
   *
   * @param orderId The order ID in format ORD-YYMMDD-NNNN
   * @return The order number, or 0 if invalid
   */
  public int extractOrderNumberFromOrderId(String orderId) {
    if (orderId != null && orderId.matches("ORD-\\d{6}-\\d{4}")) {
      String numberStr = orderId.substring(11, 15);
      return Integer.parseInt(numberStr);
    }
    return 0;
  }
}

