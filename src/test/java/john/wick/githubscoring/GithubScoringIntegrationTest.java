package john.wick.githubscoring;

import john.wick.githubscoring.domain.model.Repository;
import john.wick.githubscoring.domain.port.GithubClient;
import john.wick.githubscoring.infrastructure.client.dto.PaginatedRepositories;
import john.wick.githubscoring.infrastructure.client.errors.EmptyResultException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GithubScoringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GithubClient githubClient;

    @Test
    void searchEndpointReturnsCorrectlyFormattedResults() throws Exception {
        Repository springRepo = new Repository(
                "spring-framework",
                "The Spring Framework",
                "java",
                10000,
                5000,
                LocalDate.of(2009, 1, 1),
                LocalDate.now().minusDays(2)
        );

        Repository bootRepo = new Repository(
                "spring-boot",
                "Spring Boot",
                "java",
                20000,
                8000,
                LocalDate.of(2014, 4, 1),
                LocalDate.now().minusDays(1)
        );

        when(githubClient.searchRepositories(any()))
                .thenReturn(new PaginatedRepositories(List.of(springRepo, bootRepo), 0, 1, 2));

        mockMvc.perform(get("/api/repositories/search")
                        .param("language", "java")
                        .param("keyword", "spring")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories", hasSize(2)))
                .andExpect(jsonPath("$.repositories[0].name").exists())
                .andExpect(jsonPath("$.repositories[0].score").isNumber())
                .andExpect(jsonPath("$.totalNbRepo").value(2))
                .andExpect(jsonPath("$.totalNbPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    void handlesEmptySearchResults() throws Exception {
        when(githubClient.searchRepositories(any()))
                .thenThrow(new EmptyResultException("The search returned no result."));

        mockMvc.perform(get("/api/repositories/search")
                        .param("language", "plopfoobar")
                        .param("page", "1")
                        .param("size", "20")
                        .param("sortDirection", "desc"))
                .andExpect(status().isNoContent())
                .andExpect(content().string("Github returned no results for your search."));
    }


    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public GithubClient mockGithubClient() {
            return Mockito.mock(GithubClient.class);
        }
    }
}