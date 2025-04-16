package john.wick.githubscoring.domain.port;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;

public interface RepositorySearchService {
    RepositorySearchResultDTO searchRepositories(RepoSearchCriteria criteria);
}
