package com.doan.bepsachviet_be.service.Impl;

import com.doan.bepsachviet_be.config.MomoConfig;
import com.doan.bepsachviet_be.entity.OrderEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class MomoService {

  private final MomoConfig momoConfig;
  private final RestTemplate restTemplate = new RestTemplate();

  public Map<String, Object> createPayment(OrderEntity order) throws Exception {

    String requestId = String.valueOf(System.currentTimeMillis());
    String orderId = order.getOrderId();
    String amount = String.valueOf(order.getTotalAmount().longValue());

    // ✅ KHÔNG encode orderInfo khi ký
    String orderInfo = "Thanh toán đơn hàng " + orderId;

    Map<String, Object> params = new LinkedHashMap<>();
    params.put("partnerCode", momoConfig.getPARTNER_CODE());
    params.put("accessKey", momoConfig.getACCESS_KEY());
    params.put("requestId", requestId);
    params.put("amount", amount);
    params.put("orderId", orderId);
    params.put("orderInfo", orderInfo);
    params.put("redirectUrl", momoConfig.getREDIRECT_URL());
    params.put("ipnUrl", momoConfig.getIPN_URL());
    params.put("requestType", "captureWallet");
    params.put("extraData", "");

    // ✅ Build raw signature đúng thứ tự MoMo quy định
    String rawSignature = String.format(
        "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
        momoConfig.getACCESS_KEY(),
        amount,
        "",
        momoConfig.getIPN_URL(),
        orderId,
        orderInfo,
        momoConfig.getPARTNER_CODE(),
        momoConfig.getREDIRECT_URL(),
        requestId,
        "captureWallet"
    );

    String signature = hmacSHA256(rawSignature, momoConfig.getSECRET_KEY());
    params.put("signature", signature);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ObjectMapper mapper = new ObjectMapper();
    HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(params), headers);

    Map<String, Object> response = restTemplate.postForObject(
        momoConfig.getENDPOINT(),
        entity,
        Map.class
    );

    if (response == null || !response.containsKey("payUrl")) {
      throw new RuntimeException("Không tạo được URL thanh toán MoMo: " + response);
    }
    return response;
  }

  public boolean verifySignature(Map<String, Object> body) throws Exception {
    String rawSignature = String.format(
        "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
        momoConfig.getACCESS_KEY(),
        body.get("amount").toString(),
        body.get("extraData") == null ? "" : body.get("extraData").toString(),
        body.get("message"),
        body.get("orderId"),
        body.get("orderInfo"),
        body.get("orderType"),
        body.get("partnerCode"),
        body.get("payType"),
        body.get("requestId"),
        body.get("responseTime").toString(),
        body.get("resultCode").toString(),
        body.get("transId").toString()
    );

    String computedSignature = hmacSHA256(rawSignature, momoConfig.getSECRET_KEY());
    String receivedSignature = (String) body.get("signature");

    return computedSignature.equals(receivedSignature);
  }

  private String hmacSHA256(String data, String key) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    mac.init(secretKeySpec);
    byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    StringBuilder hash = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hash.append('0');
      hash.append(hex);
    }
    return hash.toString();
  }
}
