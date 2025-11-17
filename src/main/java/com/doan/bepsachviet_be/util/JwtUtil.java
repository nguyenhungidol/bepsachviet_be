package com.doan.bepsachviet_be.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret.key}")
  private String SECRET_KEY;

  public String generateToken(UserDetails userDetails) {
    return createToken(new HashMap<>(), userDetails.getUsername());
  }

  public String createToken(
      Map<String, Object> claims,
      String subject
  ) {
    return Jwts
        .builder()
        .setClaims(claims) // Thêm các claim phụ (nếu có)
        .setSubject(subject) // Chủ thể của token (email/username)
        .setIssuedAt(new Date(System.currentTimeMillis())) // Thời gian tạo token
        .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*10)) // Thời gian hết hạn
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // Ký token bằng Secret Key và thuật toán
        .compact(); // Xây dựng và nén token thành chuỗi
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts
        .parser()
        .setSigningKey(SECRET_KEY) // Thiết lập Secret Key để giải mã và xác thực
        .parseClaimsJws(token)
        .getBody();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }
}
