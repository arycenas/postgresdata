package com.training.postgresdata.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
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
}
