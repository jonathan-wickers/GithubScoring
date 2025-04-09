package john.wick.githubscoring.domain.port;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;

import java.util.List;

public interface RepositorySearchService {
    List<Repository> searchRepositories(RepoSearchCriteria criteria);
}
