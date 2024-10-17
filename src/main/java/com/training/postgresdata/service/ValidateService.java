package com.training.postgresdata.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.training.postgresdata.request.TokenRequest;

@Service
public class ValidateService {

    private static final String USERMANAGE = "http://usermanage-app:8080/auth/validate";
    private static final Logger log = LoggerFactory.getLogger(ValidateService.class);
    private final RestTemplate restTemplate;

    public ValidateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateTokenFromUsermanage(String token) {
        TokenRequest tokenRequest = new TokenRequest(token);

        try {
            ResponseEntity<Boolean> response = restTemplate.postForEntity(USERMANAGE, tokenRequest, Boolean.class);

            boolean isValid = response.getBody() != null && response.getBody();
            if (isValid) {
                log.info("Token is valid according to UserManage service");
            } else {
                log.warn("Token is invalid according to UserManage service");
            }
            return isValid;
        } catch (RestClientException e) {
            log.error("Error while validating token with UserManage service", e);
            return false;
        }
    }
}
