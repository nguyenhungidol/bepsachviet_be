package com.doan.bepsachviet_be.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {

    private String fullName;
    private String phoneNumber;
    private String address;
}

