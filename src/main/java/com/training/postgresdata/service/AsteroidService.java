package com.training.postgresdata.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String API_KEY = "9vaeEgNEFNmo4QMaYhdLlsSWeHkw3thvGhIiZhkp";
    private static final String URL_TEMPLATE = "https://api.nasa.gov/neo/rest/v1/feed?start_date={start_date}&end_date={end_date}&api_key={api_key}";
    private static final Logger log = LoggerFactory.getLogger(AsteroidService.class);
    private final AsteroidRepository asteroidRepository;

    public AsteroidService(AsteroidRepository asteroidRepository) {
        this.asteroidRepository = asteroidRepository;
    }

    public List<Asteroid> saveAsteroid(String startDate, String endDate, String sortBy, String sortDirection) {
        RestTemplate restTemplate = new RestTemplate();
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

        sortAsteroid(asteroidList, sortBy, sortDirection);
        log.info("Sorted asteroids by {} in {} order.", sortBy, sortDirection);

        List<Asteroid> savedAsteroids = asteroidRepository.saveAllAndFlush(asteroidList);
        log.info("Saved {} asteroids to the database.", savedAsteroids.size());

        return savedAsteroids;
    }

    private List<Asteroid> parseAsteroidData(String jsonData) {
        List<Asteroid> asteroidList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            log.info("Parsing asteroid data from NASA API.");
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

    public void sortAsteroid(List<Asteroid> asteroidList, String sortBy, String sortDirection) {
        Comparator<Asteroid> comparator;

        log.info("Sorting asteroids by {} in {} order.", sortBy, sortDirection);

        comparator = switch (sortBy.toLowerCase()) {
            case "diameter" -> Comparator.comparing(Asteroid::getDiameter);
            case "distance" -> Comparator.comparing(Asteroid::getDistance);
            case "velocity" -> Comparator.comparing(Asteroid::getVelocity);
            case "closeapproachdate" -> Comparator.comparing(asteroid -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(asteroid.getCloseApproachDate(), formatter);
            });
            case "hazardous" -> Comparator.comparing(Asteroid::getHazardous);
            default -> Comparator.comparing(Asteroid::getName);
        };

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        asteroidList.sort(comparator);
        log.info("Asteroid list sorted successfully.");
    }

    public List<Asteroid> getAllAsteroids() {
        log.info("Fetching all asteroids from the database.");
        List<Asteroid> asteroidList = asteroidRepository.findAll();
        if (asteroidList.isEmpty()) {
            log.warn("No asteroid data found in database");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No asteroid data found");
        }

        log.info("Found {} asteroids in the database.", asteroidList.size());

        return asteroidList;
    }

    public Asteroid getAsteroidById(Long id) {
        log.info("Fetching asteroid with ID: {}", id);
        return asteroidRepository.findById(id).orElseThrow(() -> {
            log.warn("Asteroid with ID: {} not found.", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Asteroid not found.");
        });
    }

    public Asteroid updateAsteroidPartially(Long id, HashMap<String, Object> updates) {
        log.info("Updating asteroid with ID: {}", id);
        Optional<Asteroid> asteroidOpt = asteroidRepository.findById(id);

        if (asteroidOpt.isPresent()) {
            Asteroid asteroid = asteroidOpt.get();
            log.info("Asteroid found: {}", asteroid);

            updates.forEach((key, value) -> {
                if (value != null) {
                    switch (key) {
                        case "name" -> {
                            if (value instanceof String string) {
                                asteroid.setName(string);
                                log.info("Updated asteroid name to: {}", string);
                            } else {
                                log.warn("Invalid data type for 'name': {}", value.getClass().getSimpleName());
                            }
                        }
                        case "diameter" -> {
                            if (value instanceof Number number) {
                                asteroid.setDiameter(number.doubleValue());
                                log.info("Updated asteroid diameter to: {}", number);
                            } else {
                                log.warn("Invalid data type for 'diameter': {}", value.getClass().getSimpleName());
                            }
                        }
                        case "distance" -> {
                            if (value instanceof Number number) {
                                asteroid.setDistance(number.doubleValue());
                                log.info("Updated asteroid distance to: {}", number);
                            } else {
                                log.warn("Invalid data type for 'distance': {}", value.getClass().getSimpleName());
                            }
                        }
                        case "velocity" -> {
                            if (value instanceof Number number) {
                                asteroid.setVelocity(number.doubleValue());
                                log.info("Updated asteroid velocity to: {}", number);
                            } else {
                                log.warn("Invalid data type for 'velocity': {}", value.getClass().getSimpleName());
                            }
                        }
                        case "isHazardous" -> {
                            if (value instanceof String string) {
                                asteroid.setHazardous(string);
                                log.info("Updated asteroid hazardous status to: {}", string);
                            } else {
                                log.warn("Invalid data type for 'isHazardous': {}", value.getClass().getSimpleName());
                            }
                        }
                        case "closeApproachDate" -> {
                            if (value instanceof String string) {
                                asteroid.setCloseApproachDate(string);
                                log.info("Updated asteroid close approach date to: {}", string);
                            } else {
                                log.warn("Invalid data type for 'closeApproachDate': {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        default -> log.warn("Unrecognized field: {}", key);
                    }
                }
            });

            Asteroid updatedAsteroid = asteroidRepository.saveAndFlush(asteroid);
            log.info("Asteroid with ID: {} successfully updated.", id);

            return updatedAsteroid;
        } else {
            log.warn("Asteroid with ID: {} not found.", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Asteroid not found.");
        }
    }

    public void deleteAsteroid(Long id) {
        try {
            log.info("Deleting asteroid with ID: {}", id);
            asteroidRepository.deleteById(id);
            log.info("Asteroid with ID: {} successfully deleted.", id);
        } catch (Exception e) {
            log.error("Error deleting asteroid with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete asteroid.");
        }
    }
}
