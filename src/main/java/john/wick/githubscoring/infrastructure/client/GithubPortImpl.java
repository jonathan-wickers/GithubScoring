package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubPort;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import john.wick.githubscoring.infrastructure.client.errors.ClientException;
import john.wick.githubscoring.infrastructure.client.errors.EmptyResultException;
import john.wick.githubscoring.infrastructure.client.errors.RateLimitException;
import john.wick.githubscoring.infrastructure.client.util.RepositoryDomainMapper;
import john.wick.githubscoring.infrastructure.client.util.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GithubPortImpl implements GithubPort {

    private static final Logger log = LoggerFactory.getLogger(GithubPortImpl.class);

    private final GithubClient client;
    private final RepositoryDomainMapper mapper;


    public GithubPortImpl(
            GithubClient client,
            RepositoryDomainMapper mapper
    ) {
        this.client = client;
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

        RepoSearchResponse response = client.searchRepositories(query, searchCriteria.page(), searchCriteria.size(), searchCriteria.sortDirection());

        if (response == null) {
            throw new EmptyResultException("The search returned no result.");
        }
        int totalNbRepo = response.getTotalCount();
        int totalPages = (int) Math.ceil((double) totalNbRepo / searchCriteria.size());

        List<Repository> repositories = response.getItems().stream()
                .map(mapper::toRepository)
                .collect(Collectors.toList());

        return new PaginatedRepositories(repositories, searchCriteria.page(), totalPages, totalNbRepo);

    }

}
