package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.AddToCartRequest;
import com.doan.bepsachviet_be.io.Request.SyncCartRequest;
import com.doan.bepsachviet_be.io.Request.UpdateCartItemRequest;
import com.doan.bepsachviet_be.io.Response.CartCountResponse;
import com.doan.bepsachviet_be.io.Response.CartItemResponse;
import com.doan.bepsachviet_be.io.Response.CartResponse;
import com.doan.bepsachviet_be.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping
  public CartResponse getCart() {
    String userEmail = getCurrentUserEmail();
    return cartService.getCart(userEmail);
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public CartItemResponse addItemToCart(@RequestBody AddToCartRequest request) {
    String userEmail = getCurrentUserEmail();
    return cartService.addItemToCart(userEmail, request);
  }

  @PutMapping("/items/{itemId}")
  public CartItemResponse updateCartItem(
      @PathVariable Long itemId,
      @RequestBody UpdateCartItemRequest request) {
    String userEmail = getCurrentUserEmail();
    return cartService.updateCartItem(userEmail, itemId, request);
  }

  @DeleteMapping("/items/{itemId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeCartItem(@PathVariable Long itemId) {
    String userEmail = getCurrentUserEmail();
    cartService.removeCartItem(userEmail, itemId);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void clearCart() {
    String userEmail = getCurrentUserEmail();
    cartService.clearCart(userEmail);
  }

  @GetMapping("/count")
  public CartCountResponse getCartCount() {
    String userEmail = getCurrentUserEmail();
    return cartService.getCartCount(userEmail);
  }

  @PostMapping("/sync")
  public CartResponse syncCart(@RequestBody SyncCartRequest request) {
    String userEmail = getCurrentUserEmail();
    return cartService.syncCart(userEmail, request);
  }

  private String getCurrentUserEmail() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserDetails) {
      return ((UserDetails) principal).getUsername();
    }

    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication");
  }
}

