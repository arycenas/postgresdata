package com.training.postgresdata.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.repository.AsteroidRepository;

public class AsteroidServiceTest {

    @Mock
    private AsteroidRepository asteroidRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AsteroidService asteroidService;

    private String sampleJson;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Sample JSON response similar to the NASA API structure
        sampleJson = """
                    {
                    "near_earth_objects": {
                        "2023-09-19": [
                            {
                                "neo_reference_id": "2465633",
                                "name": "465633 (2009 JR5)",
                                "estimated_diameter": {
                                    "meters": {
                                        "estimated_diameter_max": 0.485
                                    }
                                },
                                "close_approach_data": [
                                    {
                                        "miss_distance": {
                                            "meters": "45290298.225"
                                        },
                                        "relative_velocity": {
                                            "kilometers_per_hour": "65260.569"
                                        },
                                        "close_approach_date": "2023-09-19"
                                    }
                                ],
                                "is_potentially_hazardous_asteroid": true
                            }
                        ]
                    }
                }
                """;
    }

    @Test
    public void testSaveAsteroidFromNASAAPI() throws JsonProcessingException {
        String startDate = "2023-09-19";
        String endDate = "2023-09-20";
        String sortBy = "name";
        String sortDirection = "asc";

        // Mocking the RestTemplate response
        @SuppressWarnings("unchecked")
        ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(sampleJson);

        // Mocking the saveAll method in the repository
        when(asteroidRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        // Call the service to save asteroids
        List<Asteroid> asteroids = asteroidService.saveAsteroid(startDate, endDate, sortBy, sortDirection);

        // Verify that the repository's saveAll method was called
        verify(asteroidRepository, times(1)).saveAll(anyList());

        // Ensure that the parsed asteroid from the NASA API has the correct data
        assertNotNull(asteroids);
        assertTrue(!asteroids.isEmpty()); // Should now pass as list should not be empty
        Asteroid asteroid = asteroids.get(0);
        assertEquals("(1999 TY2)", asteroid.getName());
        assertEquals(130.0289270043, asteroid.getDiameter());
        assertEquals(0.0, asteroid.getDistance());
        assertEquals(88033.1436807948, asteroid.getVelocity());
        assertEquals("2023-09-19", asteroid.getCloseApproachDate());
        assertEquals("No", asteroid.getHazardous());
    }

    @Test
    public void testUpdateAsteroidPartially() {
        Long asteroidId = 2465633L;
        Asteroid existingAsteroid = new Asteroid();
        existingAsteroid.setId(asteroidId);
        existingAsteroid.setName("465633 (2009 JR5)");
        existingAsteroid.setDiameter(0.485);
        existingAsteroid.setDistance(45290298.225);
        existingAsteroid.setVelocity(65260.569);
        existingAsteroid.setHazardous("yes");
        existingAsteroid.setCloseApproachDate("2023-09-19");

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("name", "New Asteroid Name");
        updates.put("diameter", 0.5);

        when(asteroidRepository.findById(asteroidId)).thenReturn(Optional.of(existingAsteroid));
        when(asteroidRepository.save(any(Asteroid.class))).thenReturn(existingAsteroid);

        // Call the partial update method
        Asteroid updatedAsteroid = asteroidService.updateAsteroidPartially(asteroidId, updates);

        // Verify that the repository's save method was called
        verify(asteroidRepository, times(1)).save(existingAsteroid);

        // Check that the asteroid was updated with the new values
        assertEquals("New Asteroid Name", updatedAsteroid.getName());
        assertEquals(0.5, updatedAsteroid.getDiameter());
        assertEquals(45290298.225, updatedAsteroid.getDistance()); // unchanged
        assertEquals(65260.569, updatedAsteroid.getVelocity()); // unchanged
    }

    @Test
    public void testSortAsteroidsByHazardous() {
        Asteroid asteroid1 = new Asteroid();
        asteroid1.setName("Asteroid 1");
        asteroid1.setHazardous("yes");

        Asteroid asteroid2 = new Asteroid();
        asteroid2.setName("Asteroid 2");
        asteroid2.setHazardous("no");

        List<Asteroid> asteroidList = Arrays.asList(asteroid1, asteroid2);

        // Sort by hazardous in descending order
        asteroidService.sortAsteroid(asteroidList, "hazardous", "desc");

        // The first element should be the hazardous one
        assertEquals("yes", asteroidList.get(0).getHazardous());
    }

    @Test
    public void testSortAsteroidsByCloseApproachDate() {
        Asteroid asteroid1 = new Asteroid();
        asteroid1.setName("Asteroid 1");
        asteroid1.setCloseApproachDate("2023-09-19");

        Asteroid asteroid2 = new Asteroid();
        asteroid2.setName("Asteroid 2");
        asteroid2.setCloseApproachDate("2023-09-18");

        List<Asteroid> asteroidList = Arrays.asList(asteroid1, asteroid2);

        // Sort by closeApproachDate in ascending order
        asteroidService.sortAsteroid(asteroidList, "closeapproachdate", "asc");

        // The first element should be the one with the earlier date
        assertEquals("2023-09-18", asteroidList.get(0).getCloseApproachDate());
    }
}
