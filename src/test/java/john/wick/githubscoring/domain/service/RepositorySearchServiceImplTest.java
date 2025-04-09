package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubClient;
import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositorySearchServiceImplTest {

    @Mock
    private GithubClient githubClient;

    @Mock
    private RepositoryScoreCalculator scoreCalculator;

    private RepositorySearchServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RepositorySearchServiceImpl(githubClient, scoreCalculator);
    }

    @Test
    void resultsAreSortedByScoreDescending() {
        Repository repo1 = createRepo("repo1", 100, 50);
        Repository repo2 = createRepo("repo2", 500, 200);
        Repository repo3 = createRepo("repo3", 200, 100);

        when(scoreCalculator.calculateScore(anyInt(), anyInt(), any(), any()))
                .thenReturn(5.0).thenReturn(9.0).thenReturn(7.0);

        when(githubClient.searchRepositories(any()))
                .thenReturn(Arrays.asList(repo1, repo2, repo3));

        List<Repository> results = service.searchRepositories(
                new RepoSearchCriteria("java", null, null));

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getName()).isEqualTo("repo2");
        assertThat(results.get(1).getName()).isEqualTo("repo3");
        assertThat(results.get(2).getName()).isEqualTo("repo1");
    }

    @Test
    void nullCriteriaThrowsException() {
        assertThatThrownBy(() -> service.searchRepositories(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("criteria");
    }

    private Repository createRepo(String name, int stars, int forks) {
        return new Repository(
                name,
                "Description for " + name,
                "java",
                stars,
                forks,
                LocalDate.now().minusMonths(3),
                LocalDate.now().minusDays(7)
        );
    }
}


