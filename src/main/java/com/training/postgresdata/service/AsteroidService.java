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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.repository.AsteroidRepository;

@Service
public class AsteroidService {

    private static final String API_KEY = "9vaeEgNEFNmo4QMaYhdLlsSWeHkw3thvGhIiZhkp";
    private static final String URL_TEMPLATE = "https://api.nasa.gov/neo/rest/v1/feed?start_date={start_date}&end_date={end_date}&api_key={api_key}";

    private final AsteroidRepository asteroidRepository;

    public AsteroidService(AsteroidRepository asteroidRepository) {
        this.asteroidRepository = asteroidRepository;
    }

    public List<Asteroid> saveAsteroid(String startDate, String endDate, String sortBy, String sortDirection) {
        RestTemplate restTemplate = new RestTemplate();
        String url = URL_TEMPLATE.replace("{start_date}", startDate)
                .replace("{end_date}", endDate)
                .replace("{api_key}", API_KEY);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<Asteroid> asteroidList = parseAsteroidData(response.getBody());

        sortAsteroid(asteroidList, sortBy, sortDirection);

        return asteroidRepository.saveAllAndFlush(asteroidList);
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
                    if (asteroidNode.path("is_potentially_hazardous_asteroid").asBoolean() == true) {
                        asteroid.setHazardous("Yes");
                    } else if (asteroidNode.path("is_potentially_hazardous_asteroid").asBoolean() == false)
                        asteroid.setHazardous("No");
                    asteroid.setCloseApproachDate(asteroidNode.path("close_approach_data").get(0)
                            .path("close_approach_date").asText());

                    asteroidList.add(asteroid);
                }
            }
        } catch (JsonProcessingException e) {

        }

        return asteroidList;
    }

    public void sortAsteroid(List<Asteroid> asteroidList, String sortBy, String sortDirection) {
        Comparator<Asteroid> comparator;

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
    }

    public List<Asteroid> getAllAsteroids() {
        return asteroidRepository.findAll();
    }

    public Asteroid getAsteroidById(Long id) {
        return asteroidRepository.findById(id).orElse(null);
    }

    public Asteroid updateAsteroidPartially(Long id, HashMap<String, Object> updates) {
        Logger log = LoggerFactory.getLogger(AsteroidService.class);

        // Cari asteroid berdasarkan ID
        Optional<Asteroid> asteroidOpt = asteroidRepository.findById(id);

        log.info("Mencari asteroid dengan ID: {}", id);

        if (asteroidOpt.isPresent()) {
            Asteroid asteroid = asteroidOpt.get();
            log.info("Asteroid ditemukan: {}", asteroid);

            // Lakukan pembaruan parsial hanya untuk field yang ada di updates dan valid
            updates.forEach((var key, var value) -> {
                if (value != null) {
                    switch (key) {
                        case "name" -> {
                            if (value instanceof String string) {
                                asteroid.setName(string);
                            } else {
                                log.warn("Tipe data untuk 'name' tidak valid: {}", value.getClass().getSimpleName());
                            }
                        }
                        case "diameter" -> {
                            if (value instanceof Number number) {
                                asteroid.setDiameter(number.doubleValue());
                            } else {
                                log.warn("Tipe data untuk 'diameter' tidak valid: {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        case "distance" -> {
                            if (value instanceof Number number) {
                                asteroid.setDistance(number.doubleValue());
                            } else {
                                log.warn("Tipe data untuk 'distance' tidak valid: {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        case "velocity" -> {
                            if (value instanceof Number number) {
                                asteroid.setVelocity(number.doubleValue());
                            } else {
                                log.warn("Tipe data untuk 'velocity' tidak valid: {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        case "isHazardous" -> {
                            if (value instanceof String string) {
                                asteroid.setHazardous(string);
                            } else {
                                log.warn("Tipe data untuk 'isHazardous' tidak valid: {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        case "closeApproachDate" -> {
                            if (value instanceof String string) {
                                asteroid.setCloseApproachDate(string);
                            } else {
                                log.warn("Tipe data untuk 'closeApproachDate' tidak valid: {}",
                                        value.getClass().getSimpleName());
                            }
                        }
                        default -> log.warn("Field tidak dikenali: {}", key);
                    }
                }
            });

            // Simpan asteroid yang diperbarui ke database
            return asteroidRepository.saveAndFlush(asteroid);
        } else {
            throw new RuntimeException("Asteroid not found");
        }
    }

    public void deleteAsteroid(Long id) {
        asteroidRepository.deleteById(id);
    }
}
