package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.UserEntity;
import com.doan.bepsachviet_be.io.Request.UserRequest;
import com.doan.bepsachviet_be.io.Response.UserResponse;
import com.doan.bepsachviet_be.repository.UserRepository;
import com.doan.bepsachviet_be.service.UserService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

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
}