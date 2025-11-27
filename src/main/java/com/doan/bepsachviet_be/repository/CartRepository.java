package com.doan.bepsachviet_be.repository;
import com.doan.bepsachviet_be.entity.CartEntity;
import com.doan.bepsachviet_be.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
  Optional<CartEntity> findByUser(UserEntity user);
  Optional<CartEntity> findByUserId(Long userId);
}