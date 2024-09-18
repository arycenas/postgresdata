package com.training.postgresdata.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.repository.AsteroidRepository;

public class AsteroidServiceTest {

    @Mock
    private AsteroidRepository asteroidRepository;

    @InjectMocks
    private AsteroidService asteroidService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveAsteroid() {
        Asteroid asteroid = new Asteroid();
        asteroid.setId(1L);
        when(asteroidRepository.save(asteroid)).thenReturn(asteroid);

        Asteroid result = asteroidService.saveAsteroid(asteroid);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(asteroidRepository, times(1)).save(asteroid);
    }

    @Test
    public void testGetAllAsteroids() {
        Asteroid asteroid1 = new Asteroid();
        Asteroid asteroid2 = new Asteroid();
        List<Asteroid> asteroidList = Arrays.asList(asteroid1, asteroid2);
        when(asteroidRepository.findAll()).thenReturn(asteroidList);

        List<Asteroid> result = asteroidService.getAllAsteroids();
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(asteroidRepository, times(1)).findAll();
    }

    @Test
    public void testGetAsteroidById() {
        Asteroid asteroid = new Asteroid();
        asteroid.setId(1L);
        when(asteroidRepository.findById(1L)).thenReturn(Optional.of(asteroid));

        Optional<Asteroid> result = asteroidService.getAsteroidById(1L);
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(asteroidRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteAsteroid() {
        Long id = 1L;
        doNothing().when(asteroidRepository).deleteById(id);

        asteroidService.deleteAsteroid(id);
        verify(asteroidRepository, times(1)).deleteById(id);
    }
}
