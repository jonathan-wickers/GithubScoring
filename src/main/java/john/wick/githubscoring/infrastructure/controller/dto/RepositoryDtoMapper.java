package john.wick.githubscoring.infrastructure.controller.dto;

import john.wick.githubscoring.domain.model.Repository;

import java.util.List;

public class RepositoryDtoMapper {

    public static RepositoryDTO toDto(Repository domain) {
        return new RepositoryDTO(
                domain.getName(),
                domain.getDescription(),
                domain.getLanguage(),
                domain.getStars(),
                domain.getForks(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getScore()
        );
    }

    public static RepositorySearchResultDTO toSearchResultDto(
            List<Repository> repositories,
            int totalNbRepo,
            int totalNbPage,
            int currentPage
    ) {
        List<RepositoryDTO> repositoryDTOs = repositories.stream()
                .map(RepositoryDtoMapper::toDto)
                .toList();

        return new RepositorySearchResultDTO(repositoryDTOs, totalNbRepo, totalNbPage, currentPage);
    }
}
