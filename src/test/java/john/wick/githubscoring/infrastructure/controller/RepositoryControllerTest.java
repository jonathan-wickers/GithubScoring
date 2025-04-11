package john.wick.githubscoring.infrastructure.controller;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryControllerTest {

    @Mock
    private RepositorySearchService searchService;

    private RepositoryController controller;

    @BeforeEach
    void setUp() {
        controller = new RepositoryController(searchService);
    }

    @Test
    void returnsMatchingRepositories() {
        Repository repo = new Repository(
                "spring-boot",
                "Makes it easy to create Spring apps",
                "java",
                10000, 5000,
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2023, 1, 1)
        );
        repo.setScore(4.5);

        when(searchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenReturn(List.of(repo));

        ResponseEntity<RepositorySearchResultDTO> response = controller
                .searchRepositories("java", "2022-01-01", "spring");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().repositories()).hasSize(1);
        assertThat(response.getBody().repositories().getFirst().name()).isEqualTo("spring-boot");
        assertThat(response.getBody().repositories().getFirst().score()).isEqualTo(4.5);
    }

    @Test
    void noResultsReturnsEmptyList() {
        when(searchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenReturn(Collections.emptyList());

        ResponseEntity<RepositorySearchResultDTO> response = controller
                .searchRepositories("cobol", null, null);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().repositories()).isEmpty();
        assertThat(response.getBody().totalCount()).isZero();
        verify(searchService).searchRepositories(any(RepoSearchCriteria.class));
    }

}