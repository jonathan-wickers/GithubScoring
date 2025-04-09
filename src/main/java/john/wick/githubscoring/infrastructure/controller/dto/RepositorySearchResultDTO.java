package john.wick.githubscoring.infrastructure.controller.dto;

import java.util.List;

public record RepositorySearchResultDTO(
        List<RepositoryDTO> repositories,
        int totalCount
) {
}
