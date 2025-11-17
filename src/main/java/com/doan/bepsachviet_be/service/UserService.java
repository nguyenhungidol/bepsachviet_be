package com.doan.bepsachviet_be.service;

import com.doan.bepsachviet_be.io.Request.UserRequest;
import com.doan.bepsachviet_be.io.Response.UserResponse;
import java.util.List;

public interface UserService  {
  UserResponse createUser(UserRequest request);
  String getRoleUser(String email);
  List<UserResponse> readUsers();
  void deleteUser(String id);
}
