package com.doan.bepsachviet_be.io.Response;

import java.sql.Timestamp;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
  private String userId;
  private String email;
  private String name;
  private String phoneNumber;
  private String address;
  private Timestamp createdAt;
  private Timestamp updatedAt;
  private String role;
  private Boolean isLocked;
  private Timestamp lockedAt;
  private String lockReason;
}
