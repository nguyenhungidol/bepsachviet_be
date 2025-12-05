package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.constant.PaymentMethod;
import com.doan.bepsachviet_be.entity.OrderEntity;
import com.doan.bepsachviet_be.entity.OrderItemEntity;
import com.doan.bepsachviet_be.entity.ProductEntity;
import com.doan.bepsachviet_be.repository.OrderRepository;
import com.doan.bepsachviet_be.repository.ProductRepository;
import java.sql.Timestamp;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  // Chạy mỗi 1 phút một lần
  // CHỈ tự động hủy đơn MOMO chưa thanh toán
  // Đơn COD (CASH_ON_DELIVERY) phải được admin hủy thủ công
  @Scheduled(fixedRate = 60000)
  @Transactional
  public void cancelUnpaidOrders() {
    Timestamp fifteenMinutesAgo = new Timestamp(System.currentTimeMillis() - 15 * 60 * 1000);

    // Tìm các đơn PENDING tạo quá 15 phút
    List<OrderEntity> expiredOrders = orderRepository.findByStatusAndCreatedAtBefore(
        OrderStatus.PENDING, fifteenMinutesAgo);

    for (OrderEntity order : expiredOrders) {
      try{
        // CHỈ tự động hủy đơn MOMO, bỏ qua đơn COD
        if (order.getPaymentMethod() == PaymentMethod.CASH_ON_DELIVERY) {
          log.debug("Bỏ qua đơn COD (phải admin hủy thủ công): {}", order.getOrderId());
          continue;
        }

        order.setStatus(OrderStatus.CANCELED);
        order.setNotes("Hủy tự động do quá hạn thanh toán 15 phút");
        orderRepository.save(order);
        log.info("🚫 Đã hủy đơn hàng MoMo quá hạn: {}", order.getOrderId());

        // Hoàn lại số lượng sản phẩm trong kho
        List<OrderItemEntity> items = order.getOrderItems();
        if (items != null) {
          for (OrderItemEntity item : items) {
            try {
              ProductEntity product = item.getProduct();
              if (product == null) {
                log.warn("Không tìm thấy product cho orderItem id={}", item.getId());
                continue;
              }
              Integer currentQty = product.getStockQuantity();
              int refundQty = item.getQuantity() != null ? item.getQuantity() : 0;
              int newQty = (currentQty != null ? currentQty : 0) + refundQty;
              product.setStockQuantity(newQty);
              productRepository.save(product);
              log.info("Hoàn lại {} cho product id={}, mới: {}", refundQty, product.getId(), newQty);
            } catch (Exception ie) {
              log.error("Lỗi khi hoàn lại số lượng cho orderItem {}: {}", item.getId(), ie.getMessage());
            }
          }
        }
      }catch (Exception e){
        log.error("Lỗi khi hủy đơn hàng {}: {}", order.getOrderId(), e.getMessage());
      }
    }
  }
}
