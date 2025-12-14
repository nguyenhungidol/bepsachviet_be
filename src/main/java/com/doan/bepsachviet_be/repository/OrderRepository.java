package com.doan.bepsachviet_be.repository;

import com.doan.bepsachviet_be.constant.OrderStatus;
import com.doan.bepsachviet_be.entity.OrderEntity;
import java.sql.Timestamp;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
  Page<OrderEntity> findByUser_EmailOrderByCreatedAtDesc(String email, Pageable pageable);

  Optional<OrderEntity> findByOrderId(String orderId);

  Page<OrderEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderStatus status, Pageable pageable);

  Page<OrderEntity> findByUser_UserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  List<OrderEntity> findByStatusAndCreatedAtBefore(OrderStatus status, Timestamp timestamp);

  /**
   * Count orders created on or after the given timestamp
   * Used for generating sequential order numbers
   */
  long countByCreatedAtGreaterThanEqual(Timestamp timestamp);

  /**
   * Find the last order created on a specific date
   * Used to determine the next order number for the day
   */
  Optional<OrderEntity> findFirstByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(Timestamp timestamp);
}

