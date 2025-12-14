package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.CartEntity;
import com.doan.bepsachviet_be.entity.CartItemEntity;
import com.doan.bepsachviet_be.entity.ProductEntity;
import com.doan.bepsachviet_be.entity.UserEntity;
import com.doan.bepsachviet_be.io.Request.AddToCartRequest;
import com.doan.bepsachviet_be.io.Request.SyncCartRequest;
import com.doan.bepsachviet_be.io.Request.UpdateCartItemRequest;
import com.doan.bepsachviet_be.io.Response.CartCountResponse;
import com.doan.bepsachviet_be.io.Response.CartItemResponse;
import com.doan.bepsachviet_be.io.Response.CartResponse;
import com.doan.bepsachviet_be.repository.CartItemRepository;
import com.doan.bepsachviet_be.repository.CartRepository;
import com.doan.bepsachviet_be.repository.ProductRepository;
import com.doan.bepsachviet_be.repository.UserRepository;
import com.doan.bepsachviet_be.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public CartResponse getCart(String userEmail) {
    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = getOrCreateCart(user);
    return convertToCartResponse(cart);
  }

  @Override
  @Transactional
  public CartItemResponse addItemToCart(String userEmail, AddToCartRequest request) {
    validateAddToCartRequest(request);

    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = getOrCreateCart(user);
    ProductEntity product = getProductByProductId(request.getProductId());

    // Check if product is active (soft delete check)
    if (product.getIsActive() == null || !product.getIsActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is no longer available");
    }

    // Check stock availability
    if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is out of stock");
    }

    CartItemEntity cartItem = cartItemRepository.findByCartAndProduct(cart, product)
        .orElse(null);

    int newQuantity = request.getQuantity();
    if (cartItem != null) {
      newQuantity = cartItem.getQuantity() + request.getQuantity();
    }

    // Validate total quantity against stock
    if (newQuantity > product.getStockQuantity()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Insufficient stock. Available: " + product.getStockQuantity() + ", requested: " + newQuantity);
    }

    if (cartItem != null) {
      cartItem.setQuantity(newQuantity);
    } else {
      cartItem = CartItemEntity.builder()
          .cart(cart)
          .product(product)
          .quantity(request.getQuantity())
          .build();
      cart.getItems().add(cartItem);
    }

    cartItemRepository.save(cartItem);
    return convertToCartItemResponse(cartItem);
  }

  @Override
  @Transactional
  public CartItemResponse updateCartItem(String userEmail, Long itemId, UpdateCartItemRequest request) {
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
    }

    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = getOrCreateCart(user);

    CartItemEntity cartItem = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

    if (!cartItem.getCart().getId().equals(cart.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This cart item does not belong to you");
    }

    // Check if product is still active
    ProductEntity product = cartItem.getProduct();
    if (product.getIsActive() == null || !product.getIsActive()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is no longer available");
    }

    // Check stock availability
    if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product is out of stock");
    }

    if (request.getQuantity() > product.getStockQuantity()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Insufficient stock. Available: " + product.getStockQuantity() + ", requested: " + request.getQuantity());
    }

    cartItem.setQuantity(request.getQuantity());
    cartItemRepository.save(cartItem);
    return convertToCartItemResponse(cartItem);
  }

  @Override
  @Transactional
  public void removeCartItem(String userEmail, Long itemId) {
    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = getOrCreateCart(user);

    CartItemEntity cartItem = cartItemRepository.findById(itemId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

    if (!cartItem.getCart().getId().equals(cart.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This cart item does not belong to you");
    }

    cart.getItems().remove(cartItem);
    cartItemRepository.delete(cartItem);
  }

  @Override
  @Transactional
  public void clearCart(String userEmail) {
    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = cartRepository.findByUser(user)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

    cart.getItems().clear();
    cartItemRepository.deleteByCart(cart);
  }

  @Override
  @Transactional(readOnly = true)
  public CartCountResponse getCartCount(String userEmail) {
    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = cartRepository.findByUser(user).orElse(null);

    if (cart == null || cart.getItems().isEmpty()) {
      return CartCountResponse.builder().count(0).build();
    }

    int totalCount = cart.getItems().stream()
        .mapToInt(CartItemEntity::getQuantity)
        .sum();

    return CartCountResponse.builder().count(totalCount).build();
  }

  @Override
  @Transactional
  public CartResponse syncCart(String userEmail, SyncCartRequest request) {
    if (request.getItems() == null || request.getItems().isEmpty()) {
      return getCart(userEmail);
    }

    UserEntity user = getUserByEmail(userEmail);
    CartEntity cart = getOrCreateCart(user);

    for (SyncCartRequest.CartItemRequest itemRequest : request.getItems()) {
      if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
        continue;
      }

      ProductEntity product = productRepository.findByProductId(itemRequest.getProductId())
          .orElse(null);

      if (product == null) {
        continue;
      }

      // Skip inactive (soft deleted) products
      if (product.getIsActive() == null || !product.getIsActive()) {
        continue;
      }

      // Check stock availability
      if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
        continue; // Skip out of stock products
      }

      CartItemEntity existingItem = cartItemRepository.findByCartAndProduct(cart, product)
          .orElse(null);

      int newQuantity = itemRequest.getQuantity();
      if (existingItem != null) {
        newQuantity = existingItem.getQuantity() + itemRequest.getQuantity();
      }

      // Validate against stock
      if (newQuantity > product.getStockQuantity()) {
        // Cap quantity at available stock
        newQuantity = product.getStockQuantity();
      }

      if (existingItem != null) {
        existingItem.setQuantity(newQuantity);
        cartItemRepository.save(existingItem);
      } else {
        CartItemEntity newItem = CartItemEntity.builder()
            .cart(cart)
            .product(product)
            .quantity(newQuantity)
            .build();
        cart.getItems().add(newItem);
        cartItemRepository.save(newItem);
      }
    }

    return convertToCartResponse(cart);
  }

  private UserEntity getUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private ProductEntity getProductByProductId(String productId) {
    return productRepository.findByProductId(productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
  }

  private CartEntity getOrCreateCart(UserEntity user) {
    return cartRepository.findByUser(user)
        .orElseGet(() -> {
          CartEntity newCart = CartEntity.builder()
              .user(user)
              .build();
          return cartRepository.save(newCart);
        });
  }

  private void validateAddToCartRequest(AddToCartRequest request) {
    if (request.getProductId() == null || request.getProductId().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product ID is required");
    }
    if (request.getQuantity() == null || request.getQuantity() <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than 0");
    }
  }

  private CartItemResponse convertToCartItemResponse(CartItemEntity item) {
    ProductEntity product = item.getProduct();
    BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

    return CartItemResponse.builder()
        .itemId(item.getId())
        .productId(product.getProductId())
        .productName(product.getName())
        .productDescription(product.getDescription())
        .productImageSrc(product.getImageSrc())
        .productPrice(product.getPrice())
        .quantity(item.getQuantity())
        .subtotal(subtotal)
        .isProductActive(product.getIsActive() != null && product.getIsActive())
        .availableStock(product.getStockQuantity())
        .createdAt(item.getCreatedAt())
        .updatedAt(item.getUpdatedAt())
        .build();
  }

  private CartResponse convertToCartResponse(CartEntity cart) {
    List<CartItemResponse> items = cart.getItems().stream()
        .map(this::convertToCartItemResponse)
        .collect(Collectors.toList());

    int totalItems = cart.getItems().stream()
        .mapToInt(CartItemEntity::getQuantity)
        .sum();

    BigDecimal totalPrice = items.stream()
        .map(CartItemResponse::getSubtotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return CartResponse.builder()
        .cartId(cart.getId())
        .userId(cart.getUser().getUserId())
        .items(items)
        .totalItems(totalItems)
        .totalPrice(totalPrice)
        .createdAt(cart.getCreatedAt())
        .updatedAt(cart.getUpdatedAt())
        .build();
  }
}

