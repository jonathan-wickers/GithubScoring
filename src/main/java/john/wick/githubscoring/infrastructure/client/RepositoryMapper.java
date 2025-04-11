package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import org.springframework.stereotype.Component;

@Component
public class RepositoryMapper {

    public Repository toRepository(RepoSearchResponse.RepositoryItem dto) {
        return new Repository(
                dto.getName(),
                dto.getDescription(),
                dto.getLanguage(),
                dto.getStargazersCount(),
                dto.getForksCount(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }
}
