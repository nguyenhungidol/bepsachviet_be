package com.doan.bepsachviet_be.io.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserInfoRequest {
    private String name;
    private String phoneNumber;
    private String address;
}

