package com.training.postgresdata.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.repository.AsteroidRepository;

@Service
public class AsteroidService {

    private static final String API_KEY = System.getenv("NASA_API_KEY");
    private static final String URL_TEMPLATE = System.getenv("NASA_URI");
    private static final Logger log = LoggerFactory.getLogger(AsteroidService.class);
    private final AsteroidRepository asteroidRepository;
    private final RestTemplate restTemplate;

    public AsteroidService(AsteroidRepository asteroidRepository, RestTemplate restTemplate) {
        this.asteroidRepository = asteroidRepository;
        this.restTemplate = restTemplate;
    }

    public List<Asteroid> saveAsteroid(String startDate, String endDate) {
        String url = URL_TEMPLATE.replace("{start_date}", startDate)
                .replace("{end_date}", endDate)
                .replace("{api_key}", API_KEY);

        ResponseEntity<String> response;
        try {
            log.info("Fetching data from NASA API for dates: startDate={}, endDate={}", startDate, endDate);
            response = restTemplate.getForEntity(url, String.class);
            log.info("Response from NASA API received successfully.");
        } catch (RestClientException e) {
            log.error("Failed to retrieve data from NASA API.", e);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "NASA API is unavailable.");
        }

        List<Asteroid> asteroidList = parseAsteroidData(response.getBody());
        log.info("Parsed {} asteroids from NASA API.", asteroidList.size());

        List<Asteroid> savedAsteroids = asteroidRepository.saveAllAndFlush(asteroidList);
        log.info("Saved {} asteroids to the database.", savedAsteroids.size());

        return savedAsteroids;
    }

    private List<Asteroid> parseAsteroidData(String jsonData) {
        List<Asteroid> asteroidList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode root = objectMapper.readTree(jsonData);
            JsonNode nearEarthObjects = root.path("near_earth_objects");

            for (JsonNode dateNode : nearEarthObjects) {
                for (JsonNode asteroidNode : dateNode) {
                    Asteroid asteroid = new Asteroid();
                    asteroid.setNeoReferenceId(asteroidNode.path("neo_reference_id").asLong());
                    asteroid.setName(asteroidNode.path("name").asText());
                    asteroid.setDiameter(asteroidNode.path("estimated_diameter").path("meters")
                            .path("estimated_diameter_max").asDouble());
                    asteroid.setDistance(asteroidNode.path("close_approach_data").get(0).path("miss_distance")
                            .path("kilometers").asDouble());
                    asteroid.setVelocity(asteroidNode.path("close_approach_data").get(0).path("relative_velocity")
                            .path("kilometers_per_hour").asDouble());
                    asteroid.setHazardous(
                            asteroidNode.path("is_potentially_hazardous_asteroid").asBoolean() ? "Yes" : "No");
                    asteroid.setCloseApproachDate(asteroidNode.path("close_approach_data").get(0)
                            .path("close_approach_date").asText());

                    asteroidList.add(asteroid);
                }
            }
            log.info("Successfully parsed asteroid data.");
        } catch (JsonProcessingException e) {
            log.error("Error parsing asteroid data from NASA API.", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse NASA API response.");
        }

        return asteroidList;
    }

    public Page<Asteroid> getAllAsteroids(String sortBy, String sortDirection, int page, int size) {
        Sort.Direction direction = Sort.Direction.ASC;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Asteroid> asteroidPage = asteroidRepository.findAll(pageable);
        if (asteroidPage.isEmpty()) {
            log.warn("No asteroid data found in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No asteroid data found");
        }

        log.info("Found {} asteroids in the database.", asteroidPage.getTotalElements());

        return asteroidPage;
    }

    public Asteroid getAsteroidById(Long id) {
        log.info("Fetching asteroid with ID: {}", id);
        return asteroidRepository.findById(id).orElseThrow(() -> {
            log.warn("Asteroid with ID: {} not found.", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Asteroid not found.");
        });
    }

    public Asteroid updateAsteroidPartially(Long id, HashMap<String, Object> updates) {
        Optional<Asteroid> asteroidOpt = asteroidRepository.findById(id);

        if (asteroidOpt.isPresent()) {
            Asteroid asteroid = asteroidOpt.get();

            updates.forEach((key, value) -> {
                if (value != null) {
                    switch (key) {
                        case "name" -> {
                            if (value instanceof String string) {
                                asteroid.setName(string);
                            }
                        }
                        case "diameter" -> {
                            if (value instanceof Number number) {
                                asteroid.setDiameter(number.doubleValue());
                            }
                        }
                        case "distance" -> {
                            if (value instanceof Number number) {
                                asteroid.setDistance(number.doubleValue());
                            }
                        }
                        case "velocity" -> {
                            if (value instanceof Number number) {
                                asteroid.setVelocity(number.doubleValue());
                            }
                        }
                        case "isHazardous" -> {
                            if (value instanceof String string) {
                                asteroid.setHazardous(string);
                            }
                        }
                        case "closeApproachDate" -> {
                            if (value instanceof String string) {
                                asteroid.setCloseApproachDate(string);
                            }
                        }
                        default -> log.warn("Unrecognized field: {}", key);
                    }
                }
            });

            try {
                Asteroid updatedAsteroid = asteroidRepository.saveAndFlush(asteroid);
                log.info("Successfully updated asteroid with ID: {}", id);
                return updatedAsteroid;
            } catch (Exception e) {
                log.error("Failed to update asteroid with ID: {}. Error: {}", id, e.getMessage());
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update asteroid");
            }
        } else {
            log.warn("Asteroid with ID: {} not found.", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asteroid not found");
        }
    }

    public void deleteAsteroid(Long id) {
        try {
            asteroidRepository.deleteById(id);
            log.info("Asteroid with ID: {} successfully deleted.", id);
        } catch (Exception e) {
            log.error("Error deleting asteroid with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete asteroid.");
        }
    }
}
