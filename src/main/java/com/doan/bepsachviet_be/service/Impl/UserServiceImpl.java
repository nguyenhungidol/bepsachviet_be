package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.UserEntity;
import com.doan.bepsachviet_be.io.Request.ChangePasswordRequest;
import com.doan.bepsachviet_be.io.Request.ForgotPasswordRequest;
import com.doan.bepsachviet_be.io.Request.UpdateUserInfoRequest;
import com.doan.bepsachviet_be.io.Request.UserRequest;
import com.doan.bepsachviet_be.io.Response.UserResponse;
import com.doan.bepsachviet_be.repository.UserRepository;
import com.doan.bepsachviet_be.service.EmailService;
import com.doan.bepsachviet_be.service.UserService;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  @Override
  public UserResponse createUser(UserRequest request) {
    UserEntity newUser = convertToEntity(request);
    newUser = userRepository.save(newUser);
    return convertToResponse(newUser);
  }

  private UserResponse convertToResponse(UserEntity newUser) {
    return UserResponse.builder()
        .userId(newUser.getUserId())
        .email(newUser.getEmail())
        .name(newUser.getName())
        .phoneNumber(newUser.getPhoneNumber())
        .address(newUser.getAddress())
        .role(newUser.getRole())
        .createdAt(newUser.getCreatedAt())
        .updatedAt(newUser.getUpdatedAt())
        .build();
  }

  private UserEntity convertToEntity(UserRequest request) {
    return UserEntity.builder()
        .userId(UUID.randomUUID().toString())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .name(request.getName())
        .role(request.getRole().toUpperCase())
        .build();
  }

  @Override
  public String getRoleUser(String email) {
    UserEntity exitingUser = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for the email: " + email));
    return exitingUser.getRole();
  }

  @Override
  public List<UserResponse> readUsers() {
    return userRepository.findAll()
        .stream()
        .map(userEntity -> convertToResponse(userEntity))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteUser(String id) {
    UserEntity exitingUser = userRepository.findByUserId(id)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for the id: " + id));
    userRepository.delete(exitingUser);
  }

  @Override
  public void forgotPassword(ForgotPasswordRequest request) {
    UserEntity user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + request.getEmail()));

    // Generate reset token
    String resetToken = UUID.randomUUID().toString();
    user.setResetToken(resetToken);

    // Set token expiry to 1 hour from now
    long expiryTime = System.currentTimeMillis() + (60 * 60 * 1000); // 1 hour
    user.setResetTokenExpiry(new Timestamp(expiryTime));

    userRepository.save(user);

    // Send email
    emailService.sendPasswordResetEmail(request.getEmail(), resetToken);
  }

  @Override
  public void resetPassword(String resetToken, String newPassword) {
    UserEntity user = userRepository.findByResetToken(resetToken)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token"));

    // Check if token is expired
    if (user.getResetTokenExpiry() == null ||
        user.getResetTokenExpiry().before(new Timestamp(System.currentTimeMillis()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reset token has expired");
    }

    // Update password
    user.setPassword(passwordEncoder.encode(newPassword));
    user.setResetToken(null);
    user.setResetTokenExpiry(null);

    userRepository.save(user);
  }

  @Override
  public void changePassword(String email, ChangePasswordRequest request) {
    UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

    // Verify current password
    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }

    // Update to new password
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  @Override
  public UserResponse updateUserInfo(String email, UpdateUserInfoRequest request) {
    UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

    // Update only non-null fields
    if (request.getName() != null && !request.getName().isEmpty()) {
      user.setName(request.getName());
    }
    if (request.getPhoneNumber() != null) {
      user.setPhoneNumber(request.getPhoneNumber());
    }
    if (request.getAddress() != null) {
      user.setAddress(request.getAddress());
    }

    user = userRepository.save(user);
    return convertToResponse(user);
  }

  @Override
  public UserResponse getUserInfo(String email) {
    UserEntity user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));
    return convertToResponse(user);
  }
}

