package com.training.postgresdata.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final Logger log = LoggerFactory.getLogger(AsteroidController.class);

    private final AsteroidService asteroidService;
    private final ValidateService validateService;

    public AsteroidController(AsteroidService asteroidService, ValidateService validateService) {
        this.asteroidService = asteroidService;
        this.validateService = validateService;
    }

    private boolean validateToken(String token) {
        boolean isValid = validateService.validateTokenFromUsermanage(token);
        if (isValid) {
            log.info("Token is valid.");
        } else {
            log.warn("Token is invalid or expired.");
        }
        return isValid;
    }

    @Operation(summary = "Fetch NASA API asteroid data and save to PostgreSQL")
    @PostMapping("/save")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroids fetched and saved successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> createAsteroid(@RequestHeader("Authorization") String token,
            @RequestBody AsteroidRequest asteroidRequest) {
        log.info("Request to save asteroid data from NASA API received.");

        if (!validateToken(token.substring(7))) {
            log.warn("Unauthorized request: Invalid or expired token.");
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        List<Asteroid> asteroidList = asteroidService.saveAsteroid(
                asteroidRequest.getStartDate(),
                asteroidRequest.getEndDate());

        log.info("Successfully saved {} asteroids.", asteroidList.size());
        return ResponseEntity.ok(asteroidList);
    }

    @Operation(summary = "Get all asteroid data from PostgreSQL")
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroids fetched from PostgreSQL successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> getAllAsteroids(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        log.info("Request to get all asteroids received.");

        if (!validateToken(token.substring(7))) {
            log.warn("Unauthorized request: Invalid or expired token.");
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        List<Asteroid> asteroidList = asteroidService.getAllAsteroids(sortBy, sortDirection);
        log.info("Found {} asteroids in the database.", asteroidList.size());
        return ResponseEntity.ok(asteroidList);
    }

    @Operation(summary = "Get Asteroid data by ID")
    @GetMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid fetched by ID successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> getAsteroidById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        log.info("Request to get asteroid by ID: {} received.", id);

        if (!validateToken(token.substring(7))) {
            log.warn("Unauthorized request: Invalid or expired token.");
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        Asteroid asteroid = asteroidService.getAsteroidById(id);
        if (asteroid != null) {
            log.info("Asteroid with ID: {} found.", id);
            return ResponseEntity.ok(asteroid);
        } else {
            log.warn("Asteroid with ID: {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete asteroid data by ID")
    @DeleteMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid deleted successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<?> deleteAsteroid(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        log.info("Request to delete asteroid with ID: {} received.", id);

        if (!validateToken(token.substring(7))) {
            log.warn("Unauthorized request: Invalid or expired token.");
            return ResponseEntity.status(401).body("Invalid or expired token.");
        }

        asteroidService.deleteAsteroid(id);
        log.info("Asteroid with ID: {} deleted successfully.", id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update asteroid data by ID")
    @PutMapping("/{id}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asteroid updated successfully", content = @Content(schema = @Schema(implementation = Asteroid.class)))
    })
    public ResponseEntity<Asteroid> updateAsteroidPartially(@PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        log.info("Request to update asteroid with ID: {} received.", id);

        try {
            Asteroid updatedAsteroid = asteroidService.updateAsteroidPartially(id, new HashMap<>(updates));
            log.info("Asteroid with ID: {} updated successfully.", id);
            return ResponseEntity.ok(updatedAsteroid);
        } catch (RuntimeException e) {
            log.warn("Asteroid with ID: {} not found.", id);
            return ResponseEntity.notFound().build();
        }
    }
}
