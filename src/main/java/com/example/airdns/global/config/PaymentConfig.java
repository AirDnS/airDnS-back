package com.example.airdns.global.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PaymentConfig {

    @Value("${payment.toss.client_api_key}")
    private String testClientApiKey;

    @Value("${payment.toss.secret_api_key}")
    private String testSecretKey;

    @Value("${payment.toss.success_url}")
    private String successUrl;

    @Value("${payment.toss.fail_url}")
    private String failUrl;

    public static final String URL = "https://api/tosspayment.com/v1/payments/";
}