package john.wick.githubscoring.infrastructure.controller;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import john.wick.githubscoring.infrastructure.controller.dto.RepositoryDTO;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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

    @InjectMocks
    private RepositoryController controller;

    @Test
    void returnsMatchingRepositories() {
        RepositoryDTO repo = new RepositoryDTO(
                "spring-boot",
                "Makes it easy to create Spring apps",
                "java",
                10000, 5000,
                LocalDate.of(2022, 1, 1),
                LocalDate.of(2023, 1, 1),
                4.5
        );

        when(searchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenReturn(new RepositorySearchResultDTO(List.of(repo), 1, 1, 1));

        ResponseEntity<RepositorySearchResultDTO> response = controller
                .searchRepositories("java", "2022-01-01", "spring", 1, 20, "desc");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().repositories()).hasSize(1);
        assertThat(response.getBody().repositories().getFirst().name()).isEqualTo("spring-boot");
        assertThat(response.getBody().repositories().getFirst().score()).isEqualTo(4.5);
        assertThat(response.getBody().totalNbRepo()).isEqualTo(1);
        assertThat(response.getBody().totalNbPages()).isEqualTo(1);
        assertThat(response.getBody().currentPage()).isEqualTo(1);
    }

    @Test
    void noResultsReturnsEmptyList() {
        when(searchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenReturn(new RepositorySearchResultDTO(Collections.emptyList(), 0, 0, 0));

        ResponseEntity<RepositorySearchResultDTO> response = controller
                .searchRepositories("cobol", null, null, 1, 20, "desc");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().repositories()).isEmpty();
        assertThat(response.getBody().totalNbRepo()).isZero();
        verify(searchService).searchRepositories(any(RepoSearchCriteria.class));
    }
}