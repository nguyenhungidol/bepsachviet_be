package com.doan.bepsachviet_be.service;
import com.doan.bepsachviet_be.io.Request.AddToCartRequest;
import com.doan.bepsachviet_be.io.Request.SyncCartRequest;
import com.doan.bepsachviet_be.io.Request.UpdateCartItemRequest;
import com.doan.bepsachviet_be.io.Response.CartCountResponse;
import com.doan.bepsachviet_be.io.Response.CartItemResponse;
import com.doan.bepsachviet_be.io.Response.CartResponse;
public interface CartService {
  CartResponse getCart(String userEmail);
  CartItemResponse addItemToCart(String userEmail, AddToCartRequest request);
  CartItemResponse updateCartItem(String userEmail, Long itemId, UpdateCartItemRequest request);
  void removeCartItem(String userEmail, Long itemId);
  void clearCart(String userEmail);
  CartCountResponse getCartCount(String userEmail);
  CartResponse syncCart(String userEmail, SyncCartRequest request);
}