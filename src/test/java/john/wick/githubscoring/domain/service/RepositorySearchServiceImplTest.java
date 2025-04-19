package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubPort;
import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceImplTest {

    @Mock
    private GithubPort githubPort;

    @Mock
    private RepositoryScoreCalculator scoreCalculator;

    @InjectMocks
    private RepositorySearchServiceImpl service;

    @Test
    void nullCriteriaThrowsException() {
        assertThatThrownBy(() -> service.searchRepositories(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("criteria");
    }

    @Test
    void emptyCriteriaThrowsException() {
        RepoSearchCriteria emptyCriteria = new RepoSearchCriteria(null, null, null, 0, 0, null);

        assertThatThrownBy(() -> service.searchRepositories(emptyCriteria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one search criteria must be provided");
    }

    @Test
    void futureDateThrowsException() {
        RepoSearchCriteria criteriaWithFutureDate = new RepoSearchCriteria("java", LocalDate.now().plusDays(1));

        assertThatThrownBy(() -> service.searchRepositories(criteriaWithFutureDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Date must be in the past");
    }

    @Test
    void calculateScoresForAllRepositories() {
        RepoSearchCriteria criteria = new RepoSearchCriteria("java", null, null);

        Repository repo1 = createRepo("repo1", 1000, 500);
        Repository repo2 = createRepo("repo2", 2000, 1000);
        List<Repository> repos = List.of(repo1, repo2);

        PaginatedRepositories paginatedResult = new PaginatedRepositories(repos, 0, 1, 2);

        when(githubPort.searchRepositories(any(RepoSearchCriteria.class))).thenReturn(Optional.of(paginatedResult));
        when(scoreCalculator.calculateScore(eq(1000), eq(500), any(), any())).thenReturn(4.2);
        when(scoreCalculator.calculateScore(eq(2000), eq(1000), any(), any())).thenReturn(4.8);

        RepositorySearchResultDTO result = service.searchRepositories(criteria);

        assertThat(result.repositories()).hasSize(2);
        assertThat(result.repositories().get(0).score()).isEqualTo(4.2);
        assertThat(result.repositories().get(1).score()).isEqualTo(4.8);

        verify(scoreCalculator, times(2)).calculateScore(anyInt(), anyInt(), any(), any());
    }

    @Test
    void returnsDtoWithoutResultsIfNoResultsFound() {
        RepoSearchCriteria criteria = new RepoSearchCriteria("C", null, null);

        when(githubPort.searchRepositories(any(RepoSearchCriteria.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.searchRepositories(criteria))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No repositories found for the provided criteria");

        verify(scoreCalculator, times(0)).calculateScore(anyInt(), anyInt(), any(), any());
    }

    private Repository createRepo(String name, int stars, int forks) {
        return new Repository(
                name,
                STR."Description for \{name}",
                "java",
                stars,
                forks,
                LocalDate.now().minusMonths(3),
                LocalDate.now().minusDays(7)
        );
    }
}

