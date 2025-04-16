package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubPort;
import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import org.springframework.stereotype.Component;

@Component
public class RepositorySearchServiceImpl implements RepositorySearchService {

    private final GithubPort githubPort;

    private final RepositoryScoreCalculator calculator;

    public RepositorySearchServiceImpl(GithubPort githubPort, RepositoryScoreCalculator calculator) {
        this.githubPort = githubPort;
        this.calculator = calculator;
    }


    @Override
    public PaginatedRepositories searchRepositories(RepoSearchCriteria criteria) {
        if (criteria == null || !criteria.hasAtLeastOneCriteria()) {
            throw new IllegalArgumentException("At least one search criteria must be provided");
        }
        if (!criteria.isDateInPast()) {
            throw new IllegalArgumentException("Date must be in the past");
        }

        PaginatedRepositories response = githubPort.searchRepositories(criteria);

        for (Repository repo : response.getRepositories()) {
            repo.setScore(calculator.calculateScore(
                    repo.getStars(),
                    repo.getForks(),
                    repo.getCreatedAt(),
                    repo.getUpdatedAt()
            ));
        }
        return response;
    }

}
