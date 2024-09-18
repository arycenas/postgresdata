package com.training.postgresdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.training.postgresdata.model.Asteroid;

@Repository
public interface AsteroidRepository extends JpaRepository<Asteroid, Long> {

}
