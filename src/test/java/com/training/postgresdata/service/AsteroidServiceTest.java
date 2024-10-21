package com.training.postgresdata.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

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
                    "estimated_diameter_max": 485.0
                  }
                },
                "close_approach_data": [
                  {
                    "miss_distance": {
                      "kilometers": "45290298.225"
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

  @SuppressWarnings("unchecked")
  @Test
  public void testSaveAsteroid_Success() throws JsonProcessingException {
    String startDate = "2023-09-19";
    String endDate = "2023-09-20";

    // Mocking the RestTemplate response
    ResponseEntity<String> responseEntity = mock(ResponseEntity.class);
    when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(responseEntity);
    when(responseEntity.getBody()).thenReturn(sampleJson);

    // Mocking the saveAllAndFlush method in the repository
    when(asteroidRepository.saveAllAndFlush(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // Call the service to save asteroids
    List<Asteroid> asteroids = asteroidService.saveAsteroid(startDate, endDate);

    // Verify that the repository's saveAllAndFlush method was called
    verify(asteroidRepository, times(1)).saveAllAndFlush(anyList());

    // Ensure that the parsed asteroid from the NASA API has the correct data
    assertNotNull(asteroids);
    assertFalse(asteroids.isEmpty());
    Asteroid asteroid = asteroids.get(0);
    assertEquals("465633 (2009 JR5)", asteroid.getName());
    assertEquals(485.0, asteroid.getDiameter());
    assertEquals(45290298.225, asteroid.getDistance());
    assertEquals(65260.569, asteroid.getVelocity());
    assertEquals("2023-09-19", asteroid.getCloseApproachDate());
    assertEquals("Yes", asteroid.getHazardous());
  }

  @Test
  public void testSaveAsteroid_Failure_NASA_API_Unavailable() {
    String startDate = "2023-09-19";
    String endDate = "2023-09-20";

    // Mocking RestTemplate to throw an exception
    when(restTemplate.getForEntity(anyString(), eq(String.class)))
        .thenThrow(new RestClientException("API unavailable"));

    // Expecting a ResponseStatusException with SERVICE_UNAVAILABLE status
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      asteroidService.saveAsteroid(startDate, endDate);
    });

    assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
  }

  @Test
  public void testGetAllAsteroidsWithPagination_Success() {
    String sortBy = "name";
    String sortDirection = "asc";
    int page = 0;
    int size = 5;

    Asteroid asteroid1 = new Asteroid();
    asteroid1.setName("Asteroid A");

    Asteroid asteroid2 = new Asteroid();
    asteroid2.setName("Asteroid B");

    List<Asteroid> asteroidList = Arrays.asList(asteroid1, asteroid2);

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
    Page<Asteroid> asteroidPage = new PageImpl<>(asteroidList, pageable, asteroidList.size());

    // Mocking the repository response with Page object
    when(asteroidRepository.findAll(pageable)).thenReturn(asteroidPage);

    Page<Asteroid> result = asteroidService.getAllAsteroids(sortBy, sortDirection, page, size);

    // Verify that the repository's findAll method was called with correct Pageable
    verify(asteroidRepository, times(1)).findAll(pageable);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());
    assertEquals(1, result.getTotalPages());
    assertEquals("Asteroid A", result.getContent().get(0).getName());
  }

  @Test
  public void testGetAllAsteroidsWithPagination_NoDataFound() {
    String sortBy = "name";
    String sortDirection = "asc";
    int page = 0;
    int size = 5;

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, sortBy));
    Page<Asteroid> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    // Mocking the repository to return an empty page
    when(asteroidRepository.findAll(pageable)).thenReturn(emptyPage);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      asteroidService.getAllAsteroids(sortBy, sortDirection, page, size);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  public void testGetAsteroidById_Success() {
    Long id = 1L;
    Asteroid asteroid = new Asteroid();
    asteroid.setId(id);
    asteroid.setName("Asteroid 1");

    when(asteroidRepository.findById(id)).thenReturn(Optional.of(asteroid));

    Asteroid result = asteroidService.getAsteroidById(id);

    verify(asteroidRepository, times(1)).findById(id);
    assertNotNull(result);
    assertEquals(id, result.getId());
    assertEquals("Asteroid 1", result.getName());
  }

  @Test
  public void testGetAsteroidById_NotFound() {
    Long id = 1L;

    when(asteroidRepository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      asteroidService.getAsteroidById(id);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  public void testUpdateAsteroidPartially_Success() {
    Long id = 1L;
    Asteroid existingAsteroid = new Asteroid();
    existingAsteroid.setId(id);
    existingAsteroid.setName("Old Name");
    existingAsteroid.setDiameter(100.0);

    HashMap<String, Object> updates = new HashMap<>();
    updates.put("name", "New Name");
    updates.put("diameter", 200.0);

    when(asteroidRepository.findById(id)).thenReturn(Optional.of(existingAsteroid));
    when(asteroidRepository.saveAndFlush(any(Asteroid.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Asteroid updatedAsteroid = asteroidService.updateAsteroidPartially(id, updates);

    verify(asteroidRepository, times(1)).saveAndFlush(existingAsteroid);

    assertEquals("New Name", updatedAsteroid.getName());
    assertEquals(200.0, updatedAsteroid.getDiameter());
  }

  @Test
  public void testUpdateAsteroidPartially_NotFound() {
    Long id = 1L;
    HashMap<String, Object> updates = new HashMap<>();
    updates.put("name", "New Name");

    when(asteroidRepository.findById(id)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      asteroidService.updateAsteroidPartially(id, updates);
    });

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  public void testDeleteAsteroid_Success() {
    Long id = 1L;

    doNothing().when(asteroidRepository).deleteById(id);

    asteroidService.deleteAsteroid(id);

    verify(asteroidRepository, times(1)).deleteById(id);
  }

  @Test
  public void testDeleteAsteroid_Exception() {
    Long id = 1L;

    doThrow(new RuntimeException("Delete failed")).when(asteroidRepository).deleteById(id);

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      asteroidService.deleteAsteroid(id);
    });

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
  }
}
