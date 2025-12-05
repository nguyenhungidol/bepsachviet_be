package com.doan.bepsachviet_be.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MomoConfig {

  @Value("${momo.partnerCode}")
  private String PARTNER_CODE;

  @Value("${momo.accessKey}")
  private String ACCESS_KEY;

  @Value("${momo.secretKey}")
  private String SECRET_KEY;

  @Value("${momo.endpoint}")
  private String ENDPOINT;

  @Value("${momo.redirectUrl}")
  private String REDIRECT_URL;

  @Value("${momo.ipnUrl}")
  private String IPN_URL;

  @Value("${momo.requestType}")
  private String REQUEST_TYPE;
}
