package com.doan.bepsachviet_be.repository;
import com.doan.bepsachviet_be.entity.CartEntity;
import com.doan.bepsachviet_be.entity.CartItemEntity;
import com.doan.bepsachviet_be.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
  Optional<CartItemEntity> findByCartAndProduct(CartEntity cart, ProductEntity product);
  void deleteByCart(CartEntity cart);
}