package john.wick.githubscoring.domain.port;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;

import java.util.Optional;

public interface GithubPort {
    Optional<PaginatedRepositories> searchRepositories(RepoSearchCriteria repoSearchCriteria);
}
