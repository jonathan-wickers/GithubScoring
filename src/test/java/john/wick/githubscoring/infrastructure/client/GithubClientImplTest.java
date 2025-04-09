package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubClientImplTest {

    @Mock
    private ExecutorService executor;

    @Mock
    private RepositoryMapper mapper;

    @Mock
    private WebClient webClient;

    private WebClient.Builder webClientBuilder;
    private GithubClientImpl githubClient;

    @BeforeEach
    void setUp() {
        webClientBuilder = WebClient.builder();
        webClientBuilder = spy(webClientBuilder);
        doReturn(webClient).when(webClientBuilder).build();

        githubClient = new GithubClientImpl(
                webClientBuilder,
                "https://api.github.com",
                "test-token",
                30,
                10,
                executor,
                mapper
        );
    }

    @Test
    void searchRepositoriesReturnsRepositories() {
        RepoSearchCriteria criteria = new RepoSearchCriteria(
                "spring",
                LocalDate.of(2023, 1, 1),
                "java"
        );

        Repository repo = new Repository(
                "test-repo",
                "test-description",
                "java",
                100,
                50,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 2, 1)
        );

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(Predicate.class), any(Function.class))).thenReturn(responseSpec);

        RepoSearchResponse.RepositoryItem item = new RepoSearchResponse.RepositoryItem(
                1L, "test-repo", 100, 50,
                LocalDate.of(2023, 2, 1),
                LocalDate.of(2023, 1, 1),
                "test-description", "java"
        );

        RepoSearchResponse response = new RepoSearchResponse(
                1,
                false,
                List.of(item)
        );

        when(responseSpec.bodyToMono(RepoSearchResponse.class)).thenReturn(Mono.just(response));
        when(mapper.toRepository(any(RepoSearchResponse.RepositoryItem.class))).thenReturn(repo);

        List<Repository> result = githubClient.searchRepositories(criteria);

        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("test-repo");
    }

}