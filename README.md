# Global Loyalty Indonesia Academy Spring Boot Microservices Project

## Table of Contents

- [Introduction](#introduction)
- [Architecture Overview](#architecture-overview)
- [Services](#services)
  - [Asteroid Data Service](#asteroid-data-service)
- [Technologies Used](#technologies-used)
- [Installation](#installation)

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

#### Endpoints

- `GET /asteroid` - Get all stored asteroids (requires authentication).
- `GET /asteroid/{id}` - Get specific asteroid by ID (requires authentication).
- `POST /save` - Fetch and store asteroid data from NASA (requires authentication).
- `PUT /asteroid/{id}` - Update specific asteroid data (requires authentication).
- `DELETE /asteroid/{id}` - Delete asteroid by ID (requires authentication).

## Technologies Used

- **Spring Boot**: Core framework for developing both services.
- **PostgreSQL**: For storing asteroid data in `Asteroid Data Service`.
- **Docker**: Used to run Redis, PostgreSQL, and both services containers.
- **NASA NeoWs API**: External API for fetching asteroid data.

## Installation

### Prerequisites

- Docker installed on your system.
- Java 17+ and Maven.
