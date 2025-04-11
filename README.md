The App is a wrapper used to call the Github API to search for repositories. Based on the number of stars, forks and
recency of the updates, it calculates a score and returns everything, ordered by the highest score.

I tried to keep the code clean and separated following hexagonal architecture principles.
It should be easy to update, hopefully.

Improvements:
I am calling the Github API using CompletableFuture, I could have used structured concurrency instead, but I had
surprises with the webclient over the good old RestTemplate that I was used to and decided that done is better than
perfect.
Also, it hit me too late, but I could have paginated the results of my own API the same way Github does to give less
results but much quicker instead of creating many parallel calls, giving more results in a slower time.
Overall, it does the job, but there is room for improvements. Hopefully it will be enough to talk about it.

How to run :

./mvnw clean spring-boot:run

Run the e2e tests with :
./mvnw test -Dtest=GithubScoringE2ETest -Dspring.profiles.active=e2e

If you are using Intellij, you can use GihubScoring.http to send requests to the endpoint.
There is also a github-api.http files to call github directly.

There is also a swagger UI avaialble at http://localhost:8080/swagger-ui/index.html
THe documentation of the endpoint is lacking to say the least, I would document it properly for production but in this
context, I prefer to spend time on other things.

# GitHub Repository Scoring API

An API calling the GitHub API to search for repositories and calculate a scoring metric based on stars, forks, and
update recency.

## Overview

This application acts as a wrapper for the GitHub API, providing enhanced repository search capabilities. It calculates
a composite score for each repository based on:

- Number of stars
- Number of forks
- Recency of updates

Results are returned in descending order by score, helping you identify the most valuable repositories based on these
metrics.

## Features

- **Repository Search**: Filter by language, creation date, and keywords
- **Automatic Scoring**: Get objectively ranked results based on repository metrics
- **RESTful API**: Clean, simple endpoint structure for easy integration
- **Swagger Documentation**: Interactive API documentation

## Architecture

The project follows hexagonal architecture (ports and adapters) principles to maintain clean separation of concerns:

- **Domain Layer**: Core business logic and models
- **Application Layer**: Use cases and application services
- **Infrastructure Layer**: External interfaces (REST controllers, GitHub API client)

This architecture ensures the code is maintainable, testable, and extensible.

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+ (or use the included Maven wrapper)
- Internet connection (to access GitHub API)

### Running the Application

Using Maven wrapper:

```bash
./mvnw clean spring-boot:run
```

Or with Maven installed:

```bash
mvn clean spring-boot:run
```

The application will be available at: http://localhost:8080

### Running Tests

Run the end-to-end tests:

```bash
./mvnw test -Dtest=GithubScoringE2ETest -Dspring.profiles.active=e2e
```

## API Documentation

The API is documented using OpenAPI/Swagger UI:

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html

### Sample Requests

Using IntelliJ HTTP Client:

- Import the included `GithubScoring.http` file to send requests to the endpoint
- Use `github-api.http` to call GitHub API directly for testing

### API Endpoints

Search repositories with optional filtering and scoring.

**Parameters:**

| Parameter | Type | Default | Required | Description |
|-----------|------|---------|----------|-------------|
| `language` | String | - | No | Programming language filter (max 50 chars) |
| `createdAfter` | String | - | No | Filter repositories created after date (format: YYYY-MM-DD) |
| `keyword` | String | - | No | Search term for repository name/description (max 50 chars) |
| `page` | Integer | 1 | No | Page number (min: 1) |
| `size` | Integer | 20 | No | Results per page (max: 100) |
| `sortDirection` | String | "desc" | No | Sort order: "asc" or "desc" |

**Example Request:**
GET /api/repositories/search?language=java&createdAfter=2023-01-01&keyword=spring&page=1&size=20&sortDirection=desc

**Validation Rules:**
- At least one search criteria must be provided (language, createdAfter, or keyword)
- Date must be in format YYYY-MM-DD
- Date must be in the past
- Sort direction must be "asc" or "desc"

# Improvements:

I now just call the github API, as a wrapper, not doing extra calls. THe code is much simpler.
I need to add more tests, but there are some basics already.
