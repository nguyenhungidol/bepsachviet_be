package com.doan.bepsachviet_be.controller;

import com.doan.bepsachviet_be.io.Request.AuthRequest;
import com.doan.bepsachviet_be.io.Response.AuthResponse;
import com.doan.bepsachviet_be.service.Impl.AppUserDetailServiceImpl;
import com.doan.bepsachviet_be.service.UserService;
import com.doan.bepsachviet_be.util.JwtUtil;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AuthController {
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final AppUserDetailServiceImpl appUserDetailService;
  private final UserService userService;
  private final JwtUtil jwtUtil;

  @PostMapping("/login")
  public AuthResponse login(@RequestBody AuthRequest request) throws Exception {
    authenticate(request.getEmail(), request.getPassword());
    final UserDetails user = appUserDetailService.loadUserByUsername(request.getEmail());
    String jwtToken = jwtUtil.generateToken(user);
    String role = userService.getRoleUser(request.getEmail());
    return new AuthResponse(request.getEmail(), role, jwtToken);
  }

  private void authenticate(String email, String password) throws Exception {
    try{
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    } catch (BadCredentialsException e){
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email or password is incorrect");
    } catch (LockedException e){
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
    }
  }


  @PostMapping("/encode")
  public String encodePassword(@RequestBody Map<String, String> request){
    return passwordEncoder.encode(request.get("password"));
  }
}
