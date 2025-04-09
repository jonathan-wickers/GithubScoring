package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubClient;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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


//    @Override
//    public List<Repository> searchRepositories(RepoSearchCriteria repoSearchCriteria, int page, int resultsPerPage) {
//        String query = new GithubQueryBuilder()
//                .withKeyword(repoSearchCriteria.keyword())
//                .withLanguage(repoSearchCriteria.language())
//                .withCreatedAfter(repoSearchCriteria.createdAfter())
//                .build();
//
//        GithubRepoSearchResponse response = webClient.get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/search/repositories")
//                        .queryParam("q", query)
//                        .queryParam("sort", "stars")
//                        .queryParam("order", "desc")
//                        .queryParam("per_page", resultsPerPage)
//                        .queryParam("page", page)
//                        .build())
//                .accept(MediaType.APPLICATION_JSON)
//                .retrieve()
//                .bodyToMono(GithubRepoSearchResponse.class)
//                .onErrorResume(e -> {
//                    // Log the error
//                    System.err.println("Error retrieving repositories: " + e.getMessage());
//                    return Mono.empty();
//                })
//                .blockOptional()
//                .orElse(null);
//
//
//        if (response == null || response.getItems() == null) {
//            return Collections.emptyList();
//        }
//
//        return response.getItems().stream()
//                .map(GithubRepositoryMapper::toRepository)
//                .collect(Collectors.toList());
//
//    }

    /**
     * Searches for repositories based on a search criteria.
     * The method calls the API a first time to get the total number of results.
     * Then it creates parallel calls using completableFuture and joins the results together.
     *
     * @param searchCriteria the criteria used to filter repositories
     * @return List of repositories fitting the search criteria
     */
    public List<Repository> searchRepositories(RepoSearchCriteria searchCriteria) {

        String query = new SearchQueryBuilder()
                .withKeyword(searchCriteria.keyword())
                .withLanguage(searchCriteria.language())
                .withCreatedAfter(searchCriteria.createdAfter())
                .build();

        RepoSearchResponse firstPageResponse =
                singlePageCallToSearchAPI(query, 1, pageSize).block();

        if (firstPageResponse == null || firstPageResponse.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        int totalItems = firstPageResponse.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int pagesToFetch = Math.min(totalPages, maxPages);

        List<Repository> repositories = firstPageResponse.getItems().stream()
                .map(mapper::toRepository)
                .collect(Collectors.toList());


        if (pagesToFetch > 1) {
            List<CompletableFuture<List<Repository>>> parallelCalls = createParallelCalls(pagesToFetch, query);
            CompletableFuture.allOf(parallelCalls.toArray(new CompletableFuture[0])).join();

            for (CompletableFuture<List<Repository>> future : parallelCalls) {
                repositories.addAll(future.join());
            }

        }

        return repositories;
    }

    /**
     * Creates the parallel calls to the repository search API.
     *
     * @param pagesToFetch Number of pages to call
     * @param query        search query sent to the API
     * @return List of API calls that will be executed in the future that will contain a List of Repository objects.
     **/

    private List<CompletableFuture<List<Repository>>> createParallelCalls(int pagesToFetch, String query) {
        List<CompletableFuture<List<Repository>>> futures = new ArrayList<>();

        for (int pageNb = 2; pageNb <= pagesToFetch; pageNb++) {
            final int currentPage = pageNb;

            CompletableFuture<List<Repository>> future = CompletableFuture.supplyAsync(() -> {
                RepoSearchResponse response = singlePageCallToSearchAPI(query, currentPage, pageSize).block();
                return response != null ?
                        response.getItems().stream()
                                .map(mapper::toRepository)
                                .collect(Collectors.toList())
                        : Collections.emptyList();

            }, executor);
            futures.add(future);
        }
        return futures;
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


    /**
     * Check and handle API rate limits
     */
//    public void checkRateLimits() {
//        webClient.get()
//                .uri("/rate_limit")
//                .retrieve()
//                .bodyToMono(RatelimitException.class)
//                .subscribe(response -> {
//                    int remaining = response.getResources().getCore().getRemaining();
//                    if (remaining < 10) {
//                        logger.warn("GitHub API rate limit is getting low: {} requests remaining", remaining);
//                    }
//                });
//    }

/*
    public Flux<Repository> searchRepositoriesReactive(RepoSearchCriteria searchCriteria) {
        String query = new GithubQueryBuilder()
                .withKeyword(searchCriteria.keyword())
                .withLanguage(searchCriteria.language())
                .withCreatedAfter(searchCriteria.createdAfter())
                .build();

        return Flux.range(1, maxPages)
                .flatMap(page -> singlePageCallToSearchAPI(query, page, pageSize)
                        .flatMapIterable(response -> {
                            if (response == null || response.getItems() == null) {
                                return Collections.emptyList();
                            }
                            return response.getItems();
                        })
                        .map(GithubRepositoryMapper::toRepository)
                );
    }

    @Override
    public List<Repository> searchRepositories(RepoSearchCriteria searchCriteria) {
        String query = new GithubQueryBuilder()
                .withKeyword(searchCriteria.keyword())
                .withLanguage(searchCriteria.language())
                .withCreatedAfter(searchCriteria.createdAfter())
                .build();

        int totalResults = getTotalResultCount(query).block();
        int pagesToFetch = Math.min(maxPages, (totalResults + pageSize - 1) / pageSize);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork a subtask for each page
            List<StructuredTaskScope.Subtask<List<Repository>>> tasks = IntStream.range(1, pagesToFetch + 1)
                    .mapToObj(page -> scope.fork(() -> fetchPageBlocking(query, page)))
                    .collect(Collectors.toList());

            // Join all tasks and propagate exceptions
            scope.join();
            scope.throwIfFailed();

            // Collect and process results
            return tasks.stream()
                    .map(StructuredTaskScope.Subtask::get)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Repository search was interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Error searching repositories", e);
        }
    }

    */
    /**
     * Fetches a single page and blocks until completion (used with structured concurrency)
     *//*

    private List<Repository> fetchPageBlocking(String query, int page) {
        return singlePageCallToSearchAPI(query, page, pageSize)
                .flatMapIterable(response -> {
                    if (response == null || response.getItems() == null) {
                        return Collections.emptyList();
                    }
                    return response.getItems();
                })
                .map(GithubRepositoryMapper::toRepository)
                .collectList()
                .block();
    }

    */
/**
 * Get total count of results for pagination planning
 *//*

    private Mono<Integer> getTotalResultCount(String query) {
        return singlePageCallToSearchAPI(query, 1, 1)
                .map(GithubRepoSearchResponse::getTotalCount)
                .defaultIfEmpty(0);
    }

    */
/**
 * Fetches a single page of search results non-blocking
 *//*

    private Mono<GithubRepoSearchResponse> singlePageCallToSearchAPI(
            String searchQuery, int page, int perPage) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", searchQuery)
                        .queryParam("sort", "stars")
                        .queryParam("order", "desc")
                        .queryParam("per_page", perPage)
                        .queryParam("page", page)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GithubRepoSearchResponse.class)
                .onErrorResume(e -> {
                    // Log the error
                    System.err.println("Error retrieving repositories: " + e.getMessage());
                    return Mono.empty();
                });
    }

*/

}
