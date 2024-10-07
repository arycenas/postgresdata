package com.training.postgresdata.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.request.AsteroidRequest;
import com.training.postgresdata.service.AsteroidService;
import com.training.postgresdata.service.ValidateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/asteroid")
@Tag(name = "Asteroid Controller", description = "Operations to create, read, update, and delete asteroids from NASA API")
public class AsteroidController {

    private final AsteroidService asteroidService;
    private final ValidateService validateService;

    public AsteroidController(AsteroidService asteroidService, ValidateService validateService) {
        this.asteroidService = asteroidService;
        this.validateService = validateService;
    }

    private boolean validateToken(String token) {
        return validateService.validateTokenFromUsermanage(token);
    }

    @Operation(summary = "Fetch NASA API asteroid data and save to PostgreSQL")
    @PostMapping("/save")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroids fetched and saved successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
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

    @Operation(summary = "Get all asteroid data from PostgreSQL")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroids fetched from PostgreSQL successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> getAllAsteroids(@RequestHeader("Authorization") String token) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        return ResponseEntity.ok(asteroidService.getAllAsteroids());
    }

    @Operation(summary = "Get Asteroid data by ID")
    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid fetched by ID successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
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

    @Operation(summary = "Delete asteroid data by ID")
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid deleted successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> deleteAsteroid(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        if (!validateToken(token.substring(7))) {
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        asteroidService.deleteAsteroid(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update asteroid data by ID")
    @PutMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid updated successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<Asteroid> updateAsteroidPartially(@PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        try {
            Asteroid updatedAsteroid = asteroidService.updateAsteroidPartially(id, new HashMap<>(updates));
            return ResponseEntity.ok(updatedAsteroid);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
