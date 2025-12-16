package com.doan.bepsachviet_be.io.Request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockUserRequest {
    private String reason;
}

