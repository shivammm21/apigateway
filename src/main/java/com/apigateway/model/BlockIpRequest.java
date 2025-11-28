package com.apigateway.model;

import lombok.Data;

@Data
public class BlockIpRequest {
    private String ip;
    private String reason;
}
