package com.training.postgresdata.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private AsteroidRepository asteroidRepository;

    private static final String API_KEY = "9vaeEgNEFNmo4QMaYhdLlsSWeHkw3thvGhIiZhkp";
    private static final String URL_TEMPLATE = "https://api.nasa.gov/neo/rest/v1/feed?start_date={start_date}&end_date={end_date}&api_key={api_key}";

    public List<Asteroid> saveAsteroid(String startDate, String endDate, String sortBy, String sortDirection) {
        RestTemplate restTemplate = new RestTemplate();
        String url = URL_TEMPLATE.replace("{start_date}", startDate)
                .replace("{end_date}", endDate)
                .replace("{api_key}", API_KEY);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<Asteroid> asteroidList = parseAsteroidData(response.getBody());

        sortAsteroid(asteroidList, sortBy, sortDirection);

        return asteroidRepository.saveAll(asteroidList);
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
                    asteroid.setId(asteroidNode.path("neo_reference_id").asLong());
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
        Optional<Asteroid> asteroidOpt = asteroidRepository.findById(id);

        if (asteroidOpt.isPresent()) {
            Asteroid asteroid = asteroidOpt.get();

            updates.forEach((key, value) -> {
                switch (key) {
                    case "name" -> asteroid.setName((String) value);
                    case "diameter" -> asteroid.setDiameter((Double) value);
                    case "distance" -> asteroid.setDistance((Double) value);
                    case "velocity" -> asteroid.setVelocity((Double) value);
                    case "isHazardous" -> asteroid.setHazardous((String) value);
                    case "closeApproachDate" -> asteroid.setCloseApproachDate((String) value);
                }
            });

            return asteroidRepository.save(asteroid);
        } else {
            throw new RuntimeException("Asteroid not found");
        }
    }

    public void deleteAsteroid(Long id) {
        asteroidRepository.deleteById(id);
    }
}
