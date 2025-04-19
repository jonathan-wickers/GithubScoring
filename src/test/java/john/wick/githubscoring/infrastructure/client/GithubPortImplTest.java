package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import john.wick.githubscoring.infrastructure.client.util.RepositoryDomainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GithubPortImplTest {

    @Mock
    private GithubClient client;

    @Mock
    private RepositoryDomainMapper mapper;

    @InjectMocks
    private GithubPortImpl githubPort;

    @Test
    void searchRepositories_returnsEmptyOptional_whenNoResults() {
        RepoSearchCriteria criteria = new RepoSearchCriteria("java", LocalDate.now().minusDays(30), "test", 1, 10, "desc");
        RepoSearchResponse response = new RepoSearchResponse(0, false, Collections.emptyList());

        when(client.searchRepositories(anyString(), any(Integer.class), any(Integer.class), anyString()))
                .thenReturn(response);

        Optional<PaginatedRepositories> result = githubPort.searchRepositories(criteria);

        assertThat(result).isEmpty();
    }

    @Test
    void searchRepositories_returnsPaginatedRepositories_whenResultsExist() {
        RepoSearchCriteria criteria = new RepoSearchCriteria("java", LocalDate.now().minusDays(30), "test", 1, 10, "desc");
        RepoSearchResponse response = new RepoSearchResponse(1, false, List.of(
                new RepoSearchResponse.RepositoryItem(Long.getLong("1"), "test", 1, 1, LocalDate.now().minusDays(60), LocalDate.now().minusMonths(5), "test", "java")));

        when(client.searchRepositories(anyString(), any(Integer.class), any(Integer.class), anyString()))
                .thenReturn(response);

        Repository repository = new Repository("repo1", "description", "java", 100, 50, LocalDate.now().minusDays(365), LocalDate.now().minusDays(10));
        when(mapper.toRepository(any())).thenReturn(repository);

        Optional<PaginatedRepositories> result = githubPort.searchRepositories(criteria);

        PaginatedRepositories paginatedRepos = result.orElse(null);

        assertThat(paginatedRepos).isNotNull();
        assertThat(paginatedRepos.getRepositories()).hasSize(1);
        assertThat(paginatedRepos.getRepositories().getFirst()).isEqualTo(repository);
        assertThat(paginatedRepos.getTotalNbRepo()).isEqualTo(1);
        assertThat(paginatedRepos.gettotalNbPage()).isEqualTo(1);
        assertThat(paginatedRepos.getCurrentPage()).isEqualTo(1);
    }

    @Test
    void searchRepositories_throwsRuntimeException_whenClientThrowsException() {
        RepoSearchCriteria criteria = new RepoSearchCriteria("java", LocalDate.now().minusDays(30), "test", 1, 10, "desc");

        when(client.searchRepositories(anyString(), any(Integer.class), any(Integer.class), anyString()))
                .thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> githubPort.searchRepositories(criteria))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to search repositories from GitHub");
    }
}