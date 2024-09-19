package com.training.postgresdata.request;

import lombok.Data;

@Data
public class AsteroidRequest {

    private String startDate;
    private String endDate;
    private String sortBy;
    private String sortDirection;
}
