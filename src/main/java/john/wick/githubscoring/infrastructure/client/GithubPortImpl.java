package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubPort;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import john.wick.githubscoring.infrastructure.client.util.RepositoryDomainMapper;
import john.wick.githubscoring.infrastructure.client.util.SearchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
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

    public Optional<PaginatedRepositories> searchRepositories(RepoSearchCriteria searchCriteria) {

        String query = new SearchQueryBuilder()
                .withKeyword(searchCriteria.keyword())
                .withLanguage(searchCriteria.language())
                .withCreatedAfter(searchCriteria.createdAfter())
                .build();

        try {
            RepoSearchResponse response = client.searchRepositories(query, searchCriteria.page(), searchCriteria.size(), searchCriteria.sortDirection());


            if (response == null || response.getItems().isEmpty()) {
                return Optional.empty();
            }

            int totalNbRepo = response.getTotalCount();
            int totalPages = (int) Math.ceil((double) totalNbRepo / searchCriteria.size());

            List<Repository> repositories = response.getItems().stream()
                    .map(mapper::toRepository)
                    .collect(Collectors.toList());

            return Optional.of(new PaginatedRepositories(repositories, searchCriteria.page(), totalPages, totalNbRepo));
        } catch (Exception e) {
            log.error("Error while searching repositories in GitHub API: {}", e.getMessage());
            throw new RuntimeException("Failed to search repositories from GitHub", e);
        }


    }

}
