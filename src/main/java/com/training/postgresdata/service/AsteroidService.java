package com.training.postgresdata.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.training.postgresdata.model.Asteroid;
import com.training.postgresdata.repository.AsteroidRepository;

@Service
public class AsteroidService {

    @Autowired
    private AsteroidRepository asteroidRepository;

    public Asteroid saveAsteroid(Asteroid asteroid) {
        return asteroidRepository.save(asteroid);
    }

    public List<Asteroid> getAllAsteroids() {
        return asteroidRepository.findAll();
    }

    public Optional<Asteroid> getAsteroidById(Long id) {
        return asteroidRepository.findById(id);
    }

    public void deleteAsteroid(Long id) {
        asteroidRepository.deleteById(id);
    }
}
