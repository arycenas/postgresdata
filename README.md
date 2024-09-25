# Global Loyalty Indonesia Academy Spring Boot Microservices Project

## Table of Contents

- [Introduction](#introduction)
- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [Asteroid Data Service](#asteroid-data-service)
- [Technologies Used](#technologies-used)
  - [Endpoints](#endpoints)
    - [Get All Stored Asteroids](#get-all-asteroids)
    - [Get Specific Asteroid by ID](#get-asteroid)
    - [Fetch and Store Asteroid Data from NASA ](#save-asteroid)
    - [Update Specific Asteroid Data](#update-asteroid)
    - [Delete Asteroid by ID](#delete-asteroid)

## Introduction

This project is composed one of two microservices:

1. **Asteroid Data Service**: Fetches asteroid data from NASA's public API and performs CRUD operations on it using PostgreSQL. Access to this service is controlled through authentication from the User Management Service.

## Architecture Overview

- **Microservices**: Two independent services communicating via REST API.
- **Authentication & Authorization**: JWT-based token authentication managed by Redis.
- **Persistence**: User sessions are stored in Redis, and asteroid data is stored in PostgreSQL.
- **External API Integration**: Asteroid data is fetched from NASA's NeoWs API.

## Services

### Asteroid Data Service

This service handles:

- Fetching asteroid data from NASA's NeoWs API.
- Storing fetched data into PostgreSQL.
- Performing CRUD operations on asteroid data (create, read, update, delete).
- Validating user tokens by calling the User Management Service.

## Technologies Used

- **Spring Boot**: Core framework for developing both services.
- **PostgreSQL**: For storing asteroid data in `Asteroid Data Service`.
- **Docker**: Used to run Redis, PostgreSQL, and both services containers.
- **NASA NeoWs API**: External API for fetching asteroid data.

---

## Endpoints

| Endpoint         | Description                                                       | Method | Request Body    | Response Body  |
| ---------------- | ----------------------------------------------------------------- | ------ | --------------- | -------------- |
| `/asteroid`      | Get all stored asteroids (requires authentication)                | GET    | -               | Asteroid       |
| `/asteroid/{id}` | Get specific asteroid by ID (requires authentication)             | GET    | -               | Asteroid       |
| `/asteroid/save` | Fetch and store asteroid data from NASA (requires authentication) | POST   | AsteroidRequest | List<Asteroid> |
| `/asteroid/{id}` | Update specific asteroid data (requires authentication)           | PUT    | -               | Asteroid       |
| `/asteroid/{id}` | Delete asteroid by ID (requires authentication)                   | DELETE | -               | -              |

---

## Requests and Responses Payload

### Requests

#### Get All Asteroids Request

```sh
Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nIiwiaWF0IjoxNzI3MjMxODcyLCJleHAiOjE3MjczMTgyNzJ9.W7Qxu9bJ0DCWn95QYO5WGU0z3CnIz1EjuFbVVPdNB-A
```

#### Get Asteroid By ID Request

```sh
Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nIiwiaWF0IjoxNzI3MjMxODcyLCJleHAiOjE3MjczMTgyNzJ9.W7Qxu9bJ0DCWn95QYO5WGU0z3CnIz1EjuFbVVPdNB-A
```

#### Save Asteroid Request

```sh
Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nIiwiaWF0IjoxNzI3MjMxODcyLCJleHAiOjE3MjczMTgyNzJ9.W7Qxu9bJ0DCWn95QYO5WGU0z3CnIz1EjuFbVVPdNB-A

{
    "startDate": "2024-09-02",
    "endDate": "2024-09-09",
    "sortBy": "name",
    "sortDirection": "asc"
}
```

#### Update Asteroid By ID Request

```sh
Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nIiwiaWF0IjoxNzI3MjMxODcyLCJleHAiOjE3MjczMTgyNzJ9.W7Qxu9bJ0DCWn95QYO5WGU0z3CnIz1EjuFbVVPdNB-A

{
    "name": "testing",
    "diameter": 9084
}
```

#### Delete Asteroid By ID Request

```sh
Bearer: eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0aW5nIiwiaWF0IjoxNzI3MjMxODcyLCJleHAiOjE3MjczMTgyNzJ9.W7Qxu9bJ0DCWn95QYO5WGU0z3CnIz1EjuFbVVPdNB-A
```

### Responses

#### Get All Asteroids Response

```sh
[
  {
        "id": 2,
        "name": "(2006 WE4)",
        "diameter": 986.3702813054,
        "distance": 5.278499844418508E7,
        "velocity": 57520.1275710815,
        "hazardous": "No",
        "closeApproachDate": "2024-09-08"
    },
    {
        "id": 3,
        "name": "(2007 RX8)",
        "diameter": 73.7972160683,
        "distance": 7172865.997349973,
        "velocity": 25086.7943393211,
        "hazardous": "No",
        "closeApproachDate": "2024-09-02"
    },
    {
        "id": 4,
        "name": "(2007 SV1)",
        "diameter": 68.2401509401,
        "distance": 7.104021721790516E7,
        "velocity": 85208.2327156723,
        "hazardous": "No",
        "closeApproachDate": "2024-09-06"
    },
    {
        "id": 5,
        "name": "(2007 VW83)",
        "diameter": 89.9580388169,
        "distance": 5.904721521815408E7,
        "velocity": 63405.9038073701,
        "hazardous": "No",
        "closeApproachDate": "2024-09-08"
    },
    {
        "id": 6,
        "name": "(2007 YJ1)",
        "diameter": 51.7654482198,
        "distance": 3.880583562477066E7,
        "velocity": 10808.698347775,
        "hazardous": "No",
        "closeApproachDate": "2024-09-07"
    },
    {
        "id": 7,
        "name": "(2008 EM)",
        "diameter": 69.5088300671,
        "distance": 2.2802691669465028E7,
        "velocity": 47705.8142605918,
        "hazardous": "No",
        "closeApproachDate": "2024-09-02"
    }
]
```

#### Get Asteroid By ID Response

```sh
{
    "id": 2,
    "name": "(2006 WE4)",
    "diameter": 986.3702813054,
    "distance": 5.278499844418508E7,
    "velocity": 57520.1275710815,
    "hazardous": "No",
    "closeApproachDate": "2024-09-08"
}
```

#### Save Asteroid Response

```sh
[
  {
        "id": 2,
        "name": "(2006 WE4)",
        "diameter": 986.3702813054,
        "distance": 5.278499844418508E7,
        "velocity": 57520.1275710815,
        "hazardous": "No",
        "closeApproachDate": "2024-09-08"
    },
    {
        "id": 3,
        "name": "(2007 RX8)",
        "diameter": 73.7972160683,
        "distance": 7172865.997349973,
        "velocity": 25086.7943393211,
        "hazardous": "No",
        "closeApproachDate": "2024-09-02"
    },
    {
        "id": 4,
        "name": "(2007 SV1)",
        "diameter": 68.2401509401,
        "distance": 7.104021721790516E7,
        "velocity": 85208.2327156723,
        "hazardous": "No",
        "closeApproachDate": "2024-09-06"
    },
    {
        "id": 5,
        "name": "(2007 VW83)",
        "diameter": 89.9580388169,
        "distance": 5.904721521815408E7,
        "velocity": 63405.9038073701,
        "hazardous": "No",
        "closeApproachDate": "2024-09-08"
    },
    {
        "id": 6,
        "name": "(2007 YJ1)",
        "diameter": 51.7654482198,
        "distance": 3.880583562477066E7,
        "velocity": 10808.698347775,
        "hazardous": "No",
        "closeApproachDate": "2024-09-07"
    },
    {
        "id": 7,
        "name": "(2008 EM)",
        "diameter": 69.5088300671,
        "distance": 2.2802691669465028E7,
        "velocity": 47705.8142605918,
        "hazardous": "No",
        "closeApproachDate": "2024-09-02"
    }
]
```

#### Update Asteroid By ID Response

```sh
{
    "id": 2,
    "name": "testing",
    "diameter": 9084.0,
    "distance": 5.278499844418508E7,
    "velocity": 57520.1275710815,
    "hazardous": "No",
    "closeApproachDate": "2024-09-08"
}
```

#### Delete Asteroid By ID Response

```sh
Successfully delete Asteroid
```
