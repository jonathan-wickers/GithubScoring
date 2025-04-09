package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubClient;
import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class RepositorySearchServiceImpl implements RepositorySearchService {

    private final GithubClient githubClient;

    private final RepositoryScoreCalculator calculator;

    public RepositorySearchServiceImpl(GithubClient githubClient, RepositoryScoreCalculator calculator) {
        this.githubClient = githubClient;
        this.calculator = calculator;
    }


    @Override
    public List<Repository> searchRepositories(RepoSearchCriteria criteria) {
        if (criteria == null || !criteria.hasAtLeastOneCriteria()) {
            throw new IllegalArgumentException("At least one search criteria must be provided");
        }
        if (!criteria.isDateInPast()) {
            throw new IllegalArgumentException("Date must be in the past");
        }

        List<Repository> repositories = githubClient.searchRepositories(criteria);

        for (Repository repo : repositories) {
            repo.setScore(calculator.calculateScore(
                    repo.getStars(),
                    repo.getForks(),
                    repo.getCreatedAt(),
                    repo.getUpdatedAt()
            ));
        }

        return repositories.stream()
                .sorted(Comparator.comparing(Repository::getScore).reversed())
                .toList();
    }

}
