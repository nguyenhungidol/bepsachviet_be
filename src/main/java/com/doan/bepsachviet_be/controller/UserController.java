package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.ChangePasswordRequest;
import com.doan.bepsachviet_be.io.Request.ForgotPasswordRequest;
import com.doan.bepsachviet_be.io.Request.LockUserRequest;
import com.doan.bepsachviet_be.io.Request.ResetPasswordRequest;
import com.doan.bepsachviet_be.io.Request.UpdateUserInfoRequest;
import com.doan.bepsachviet_be.io.Request.UserRequest;
import com.doan.bepsachviet_be.io.Response.MessageResponse;
import com.doan.bepsachviet_be.io.Response.UserResponse;
import com.doan.bepsachviet_be.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @PostMapping("/registers")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse registerUser(@RequestBody UserRequest request){
    try{
      return userService.createUser(request);
    }catch (Exception e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create user" + e.getMessage());
    }
  }

  @GetMapping("/admin/users")
  public List<UserResponse> readUsers(){
    return userService.readUsers();
  }

  @DeleteMapping("/admin/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable String id){
    try{
      userService.deleteUser(id);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
    }
  }

  // Password Reset Endpoints
  @PostMapping("/forgot-password")
  public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    try {
      userService.forgotPassword(request);
      return new MessageResponse("Password reset link has been sent to your email");
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to process request: " + e.getMessage());
    }
  }

  @PostMapping("/reset-password")
  public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    try {
      userService.resetPassword(request.getResetToken(), request.getNewPassword());
      return new MessageResponse("Password has been reset successfully");
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to reset password: " + e.getMessage());
    }
  }

  @PostMapping("/change-password")
  public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
    try {
      String email = principal.getName();
      userService.changePassword(email, request);
      return new MessageResponse("Password has been changed successfully");
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to change password: " + e.getMessage());
    }
  }

  // User Info Endpoints
  @GetMapping("/user/profile")
  public UserResponse getUserProfile(Principal principal) {
    try {
      String email = principal.getName();
      return userService.getUserInfo(email);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + e.getMessage());
    }
  }

  @PutMapping("/user/profile")
  public UserResponse updateUserProfile(@RequestBody UpdateUserInfoRequest request, Principal principal) {
    try {
      String email = principal.getName();
      return userService.updateUserInfo(email, request);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to update profile: " + e.getMessage());
    }
  }

  // Lock/Unlock User Endpoints (Admin only)
  @PostMapping("/admin/users/{userId}/lock")
  public UserResponse lockUser(@PathVariable String userId, @RequestBody(required = false) LockUserRequest request) {
    try {
      String reason = (request != null && request.getReason() != null) ? request.getReason() : "Account locked by administrator";
      return userService.lockUser(userId, reason);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to lock user: " + e.getMessage());
    }
  }

  @PostMapping("/admin/users/{userId}/unlock")
  public UserResponse unlockUser(@PathVariable String userId) {
    try {
      return userService.unlockUser(userId);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to unlock user: " + e.getMessage());
    }
  }
}
