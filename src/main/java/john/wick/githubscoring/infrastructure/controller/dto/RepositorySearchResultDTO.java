package john.wick.githubscoring.infrastructure.controller.dto;

import java.util.List;

public record RepositorySearchResultDTO(
        List<RepositoryDTO> repositories,
        int totalNbRepo,
        int totalNbPages,
        int currentPage
) {
}
