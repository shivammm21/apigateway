package com.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteConfigModel {
    private String path;       // e.g. /users/**
    private String serviceUrl; // e.g. http://user-service:8081
}
