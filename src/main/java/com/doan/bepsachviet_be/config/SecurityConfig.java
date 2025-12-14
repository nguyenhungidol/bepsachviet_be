package com.doan.bepsachviet_be.config;

import com.doan.bepsachviet_be.filters.JwtRequestFilter;
import com.doan.bepsachviet_be.service.Impl.AppUserDetailServiceImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final AppUserDetailServiceImpl appUserDetailService;
  private final JwtRequestFilter jwtRequestFilter;

  @Bean
  public SecurityFilterChain securityFilterChain (HttpSecurity httpSecurity) throws Exception {
    httpSecurity.cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth.requestMatchers("/login", "/encode", "/registers", "/upload", "/forgot-password", "/reset-password").permitAll()
            .requestMatchers(HttpMethod.GET, "/categories", "/categories/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/posts", "/posts/**").permitAll()
            .requestMatchers("/payment/momo/ipn-handler", "/payment/momo/return").permitAll()
            .requestMatchers("/payment/momo/create").authenticated()
            .requestMatchers("/ws/**").permitAll() // WebSocket endpoint
            .requestMatchers("/chat/**").permitAll() // Chat endpoints (authentication checked in controller)
            .requestMatchers("/admin/chat/**").hasRole("ADMIN") // Admin chat endpoints
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/cart/**").authenticated()
            .requestMatchers(HttpMethod.POST, "/orders").authenticated()
            .requestMatchers("/orders/**").authenticated()
            .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsFilter corsFilter(){
    return new CorsFilter((CorsConfigurationSource) corsConfigurationSource());
  }

  private UrlBasedCorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("http://localhost:5173"));
    configuration.setAllowedMethods(List.of("POST", "GET", "DELETE", "PUT", "PATCH", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(){
    DaoAuthenticationProvider providential = new DaoAuthenticationProvider();
    providential.setUserDetailsService(appUserDetailService);
    providential.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(providential);
  }
}
