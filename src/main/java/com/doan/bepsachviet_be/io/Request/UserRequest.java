package com.doan.bepsachviet_be.io.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {
  private String email;
  private String password;
  private String name;
  private String role;
}
