package john.wick.githubscoring.infrastructure.controller.dto;

import java.time.LocalDate;

public record RepositoryDTO(
        String name,
        String description,
        String language,
        int stars,
        int forks,
        LocalDate createdAt,
        LocalDate updatedAt,
        double score
) {
}
