package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.entity.UserEntity;
import com.doan.bepsachviet_be.repository.UserRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserEntity existingUser = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Email not found for the email: " + email));

    // Check if user account is locked
    if (Boolean.TRUE.equals(existingUser.getIsLocked())) {
      String lockMessage = existingUser.getLockReason() != null
          ? "Account is locked: " + existingUser.getLockReason()
          : "Account is locked";
      throw new LockedException(lockMessage);
    }

    String role = existingUser.getRole();
    // Ensure role has ROLE_ prefix for Spring Security hasRole() to work
    if (role != null && !role.startsWith("ROLE_")) {
      role = "ROLE_" + role;
    }
    return new User(existingUser.getEmail(), existingUser.getPassword(), Collections.singleton(new SimpleGrantedAuthority(role)));
  }
}
