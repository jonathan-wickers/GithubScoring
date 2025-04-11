package john.wick.githubscoring.infrastructure.controller;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import john.wick.githubscoring.infrastructure.controller.dto.RepositoryMapper;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/repositories")
@Validated
public class RepositoryController implements RepositoryControllerAPI {
    private final RepositorySearchService repositorySearchService;

    public RepositoryController(RepositorySearchService repositorySearchService) {
        this.repositorySearchService = repositorySearchService;
    }

    private static LocalDate getCreatedAtParam(String createdAfter) {
        LocalDate createdAfterDate = null;
        if (createdAfter != null && !createdAfter.isBlank()) {
            createdAfterDate = LocalDate.parse(createdAfter);
        }
        return createdAfterDate;
    }

    @GetMapping("/search")
    public ResponseEntity<RepositorySearchResultDTO> searchRepositories(
            @RequestParam(required = false) @Size(max = 50)
            String language,
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format YYYY-MM-DD")
            String createdAfter,
            @RequestParam(required = false) @Size(max = 50)
            String keyword) {

        LocalDate createdAfterAsDate = getCreatedAtParam(createdAfter);

        RepoSearchCriteria criteria = new RepoSearchCriteria(language, createdAfterAsDate, keyword);
        if (criteria.hasAtLeastOneCriteria()) {
            List<Repository> repositories = repositorySearchService.searchRepositories(criteria);
            RepositorySearchResultDTO resultDto = RepositoryMapper.toSearchResultDto(repositories);
            return ResponseEntity.ok(resultDto);
        } else {
            throw new IllegalArgumentException("At least one search criteria must be provided");
        }
    }
}

