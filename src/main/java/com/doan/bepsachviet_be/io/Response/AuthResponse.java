package com.doan.bepsachviet_be.io.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
  private String email;
  private String role;
  private String token;
}
