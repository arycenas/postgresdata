package com.training.postgresdata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.training.postgresdata.request.TokenRequest;

@Service
public class ValidateService {

    private static final String USERMANAGE = "http://localhost:8080/auth/validate";

    @Autowired
    private RestTemplate restTemplate;

    public boolean validateTokenFromUsermanage(String token) {
        TokenRequest tokenRequest = new TokenRequest(token);
        ResponseEntity<Boolean> response = restTemplate.postForEntity(USERMANAGE, tokenRequest, Boolean.class);

        return response.getBody() != null && response.getBody();
    }
}
