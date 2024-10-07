package com.training.postgresdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_approach_data")
public class Asteroid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "neo_reference_id")
    private Long neoReferenceId;
    @Column(name = "name")
    private String name;
    @Column(name = "estimated_diameter")
    private double diameter;
    @Column(name = "miss_distance")
    private double distance;
    @Column(name = "relative_velocity")
    private double velocity;
    @Column(name = "is_potentially_hazardous")
    private String hazardous;
    @Column(name = "close_approach_date")
    private String closeApproachDate;

    public Asteroid() {
    }

    public Asteroid(Long id, Long neoReferenceId, String name, double diameter, double distance, double velocity,
            String hazardous, String closeApproachDate) {
        this.id = id;
        this.neoReferenceId = neoReferenceId;
        this.name = name;
        this.diameter = diameter;
        this.distance = distance;
        this.velocity = velocity;
        this.hazardous = hazardous;
        this.closeApproachDate = closeApproachDate;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNeoReferenceId() {
        return this.neoReferenceId;
    }

    public void setNeoReferenceId(Long neoReferenceId) {
        this.neoReferenceId = neoReferenceId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDiameter() {
        return this.diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    public double getDistance() {
        return this.distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getVelocity() {
        return this.velocity;
    }

    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }

    public String getHazardous() {
        return this.hazardous;
    }

    public void setHazardous(String hazardous) {
        this.hazardous = hazardous;
    }

    public String getCloseApproachDate() {
        return this.closeApproachDate;
    }

    public void setCloseApproachDate(String closeApproachDate) {
        this.closeApproachDate = closeApproachDate;
    }
}
