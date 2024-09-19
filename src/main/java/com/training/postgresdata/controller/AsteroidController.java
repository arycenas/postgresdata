package com.training.postgresdata.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.request.AsteroidRequest;
import com.training.postgresdata.service.AsteroidService;
import com.training.postgresdata.service.ValidateService;

@RestController
@RequestMapping("/asteroid")
public class AsteroidController {

    @Autowired
    private AsteroidService asteroidService;

    @Autowired
    private ValidateService validateService;

    private boolean validateToken(String token) {
        return validateService.validateTokenFromUsermanage(token);
    }

    @PostMapping("/save")
    public ResponseEntity<?> createAsteroid(@RequestHeader("Authorization") String token,
            @RequestBody AsteroidRequest asteroidRequest) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        List<Asteroid> asteroidList = asteroidService.saveAsteroid(
                asteroidRequest.getStartDate(),
                asteroidRequest.getEndDate(),
                asteroidRequest.getSortBy(),
                asteroidRequest.getSortDirection());

        return ResponseEntity.ok(asteroidList);
    }

    @GetMapping
    public ResponseEntity<?> getAllAsteroids(@RequestHeader("Authorization") String token) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        return ResponseEntity.ok(asteroidService.getAllAsteroids());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAsteroidById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        Asteroid asteroid = asteroidService.getAsteroidById(id);
        if (asteroid != null) {
            return ResponseEntity.ok(asteroid);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsteroid(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        asteroidService.deleteAsteroid(id);
        return ResponseEntity.noContent().build();
    }
}
