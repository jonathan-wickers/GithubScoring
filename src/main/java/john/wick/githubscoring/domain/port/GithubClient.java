package john.wick.githubscoring.domain.port;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;

public interface GithubClient {
    PaginatedRepositories searchRepositories(RepoSearchCriteria repoSearchCriteria);
}
