package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.domain.model.RepoSearchCriteria;
import john.wick.githubscoring.domain.port.RepositorySearchService;
import john.wick.githubscoring.infrastructure.controller.RepositoryController;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;


@SpringBootTest
class GithubClientTest {

    @MockBean
    private RepositorySearchService repositorySearchService;

    @Autowired
    private RepositoryController controller;

    @Test
    public void testRateLimitHandledGracefully() {
        Mockito.when(repositorySearchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenThrow(new ResponseStatusException(TOO_MANY_REQUESTS, "Rate limit exceeded"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.searchRepositories(
                        "Java",
                        "2023-01-01",
                        "Spring",
                        1,
                        20,
                        "desc"
                )
        );

        // Verify the HTTP status and error message
        assertEquals(TOO_MANY_REQUESTS, exception.getStatusCode());
        assertEquals("Rate limit exceeded", exception.getReason());
    }

    @Test
    public void testSearchWithValidResponse() {
        RepositorySearchResultDTO mockPaginatedRepositories = new RepositorySearchResultDTO(
                Collections.emptyList(),
                0,
                0,
                0
        );

        Mockito.when(repositorySearchService.searchRepositories(any(RepoSearchCriteria.class)))
                .thenReturn(mockPaginatedRepositories);

        ResponseEntity<RepositorySearchResultDTO> response = controller.searchRepositories(
                "Java",
                "2023-01-01",
                "Spring",
                1,
                20,
                "desc"
        );

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0, response.getBody().totalNbRepo());
    }

}