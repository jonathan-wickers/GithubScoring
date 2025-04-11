package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubClient;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import john.wick.githubscoring.infrastructure.client.errors.ClientException;
import john.wick.githubscoring.infrastructure.client.errors.RateLimitException;
import john.wick.githubscoring.infrastructure.client.util.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Component
public class GithubClientImpl implements GithubClient {

    private static final Logger log = LoggerFactory.getLogger(GithubClientImpl.class);


    private final WebClient webClient;
    private final ExecutorService executor;
    private final int pageSize;
    private final int maxPages;
    private final RepositoryMapper mapper;


    public GithubClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${github.api.base-url}") String apiBaseUrl,
            @Value("${github.api.token:}") String apiToken,
            @Value("${github.api.page-size}") int pageSize,
            @Value("${github.api.max-pages}") int maxPages,
            ExecutorService executor,
            RepositoryMapper mapper
    ) {

        WebClient.Builder builder = webClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github.v3+json")
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(configurer -> configurer
                                        .defaultCodecs()
                                        .maxInMemorySize(2 * 1024 * 1024)) // Receives up to 2MB data
                                .build());

        if (apiToken != null && !apiToken.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "token " + apiToken);
        }
        this.executor = executor;
        this.pageSize = pageSize;
        this.maxPages = maxPages;
        this.webClient = builder.build();
        this.mapper = mapper;

    }

    /**
     * Searches for repositories based on a search criteria.
     * The method calls the API a first time to get the total number of results.
     * Then it creates parallel calls using completableFuture and joins the results together.
     *
     * @param searchCriteria the criteria used to filter repositories
     * @return List of repositories fitting the search criteria
     */
    public PaginatedRepositories searchRepositories(RepoSearchCriteria searchCriteria) {

        String query = new SearchQueryBuilder()
                .withKeyword(searchCriteria.keyword())
                .withLanguage(searchCriteria.language())
                .withCreatedAfter(searchCriteria.createdAfter())
                .build();

        RepoSearchResponse response =
                singlePageCallToSearchAPI(query, searchCriteria.page(), searchCriteria.size()).block();

        int totalNbRepo = response.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalNbRepo / searchCriteria.size());

        List<Repository> repositories = response.getItems().stream()
                .map(mapper::toRepository)
                .collect(Collectors.toList());

        return new PaginatedRepositories(repositories, searchCriteria.page(), totalPages, totalNbRepo);

    }

    /**
     * Makes a single-page call to the repository search endpoint.
     *
     * @param searchQuery the search query used to filter repositories
     * @param page        the number of pages to get
     * @param perPage     the number of repositories to get per page
     * @return a Mono of RepoSearchResponse
     */
    private Mono<RepoSearchResponse> singlePageCallToSearchAPI(
            String searchQuery, int page, int perPage) {

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", searchQuery)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                        return Mono.error(new RateLimitException("GitHub API rate limit exceeded"));
                    } else if (response.statusCode() == HttpStatus.FORBIDDEN) {
                        return response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Forbidden response from API: {}", errorBody);
                                    return Mono.error(new ClientException("API access forbidden: " + errorBody));
                                });
                    }
                    return Mono.error(new ClientException("Client error: " + response.statusCode()));
                })
                .bodyToMono(RepoSearchResponse.class)
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                        .filter(ex -> ex instanceof IOException))
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(e -> {
                    if (e instanceof RateLimitException) {
                        log.warn("Rate limit exceeded on API", e);
                        return Mono.empty();
                    } else {
                        log.error("Error occurred while calling the API: {}", e.getMessage(), e);
                        return Mono.empty();
                    }

                });
    }

}
