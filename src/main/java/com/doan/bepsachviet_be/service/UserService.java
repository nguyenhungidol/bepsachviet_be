package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.io.Request.ChangePasswordRequest;
import com.doan.bepsachviet_be.io.Request.ForgotPasswordRequest;
import com.doan.bepsachviet_be.io.Request.UpdateUserInfoRequest;
import com.doan.bepsachviet_be.io.Request.UserRequest;
import com.doan.bepsachviet_be.io.Response.UserResponse;
import java.util.List;

public interface UserService {
  UserResponse createUser(UserRequest request);
  String getRoleUser(String email);
  List<UserResponse> readUsers();
  void deleteUser(String id);

  // Password reset features
  void forgotPassword(ForgotPasswordRequest request);
  void resetPassword(String resetToken, String newPassword);
  void changePassword(String email, ChangePasswordRequest request);

  // Update user info
  UserResponse updateUserInfo(String email, UpdateUserInfoRequest request);
  UserResponse getUserInfo(String email);

  // Lock/Unlock user
  UserResponse lockUser(String userId, String reason);
  UserResponse unlockUser(String userId);
  boolean isUserLocked(String email);
}
