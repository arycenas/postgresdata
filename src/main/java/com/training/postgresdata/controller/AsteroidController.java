package com.training.postgresdata.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.service.AsteroidService;

@RestController
@RequestMapping("/asteroid")
public class AsteroidController {

    @Autowired
    private AsteroidService asteroidService;

    @PostMapping("/save")
    public Asteroid createAsteroid(@RequestBody Asteroid asteroid) {
        return asteroidService.saveAsteroid(asteroid);
    }

    @GetMapping
    public List<Asteroid> getAllAsteroids() {
        return asteroidService.getAllAsteroids();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asteroid> getAsteroidById(@PathVariable Long id) {
        Optional<Asteroid> asteroid = asteroidService.getAsteroidById(id);
        if (asteroid.isPresent()) {
            return ResponseEntity.ok(asteroid.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asteroid> updateAsteroid(@PathVariable Long id, @RequestBody Asteroid asteroidDetails) {
        Optional<Asteroid> asteroid = asteroidService.getAsteroidById(id);
        if (asteroid.isPresent()) {
            Asteroid existAsteroid = asteroid.get();
            existAsteroid.setName(asteroidDetails.getName());
            existAsteroid.setDiameter(asteroidDetails.getDiameter());
            existAsteroid.setDistance(asteroidDetails.getDistance());
            existAsteroid.setVelocity(asteroidDetails.getVelocity());

            Asteroid updatedAsteroid = asteroidService.saveAsteroid(existAsteroid);

            return ResponseEntity.ok(updatedAsteroid);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsteroid(@PathVariable Long id) {
        Optional<Asteroid> asteroid = asteroidService.getAsteroidById(id);
        if (asteroid.isPresent()) {
            asteroidService.deleteAsteroid(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
