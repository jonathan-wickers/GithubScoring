package john.wick.githubscoring.infrastructure.controller.dto;

import john.wick.githubscoring.domain.model.Repository;

import java.util.List;
import java.util.stream.Collectors;

public class RepositoryMapper {

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

    public static List<RepositoryDTO> toDtoList(List<Repository> domains) {
        return domains.stream()
                .map(RepositoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public static RepositorySearchResultDTO toSearchResultDto(List<Repository> domains) {
        List<RepositoryDTO> dtos = toDtoList(domains);
        return new RepositorySearchResultDTO(dtos, dtos.size());
    }
}
