package john.wick.githubscoring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("e2e")
class GithubScoringE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchPopularJavaRepositories() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/repositories/search")
                        .param("language", "java")
                        .param("keyword", "spring")
                        .param("createdAfter", "2020-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.totalCount").isNumber())
                .andExpect(jsonPath("$.repositories[*].name").exists())
                .andExpect(jsonPath("$.repositories[*].description").exists())
                .andExpect(jsonPath("$.repositories[*].language").exists())
                .andExpect(jsonPath("$.repositories[*].stars").exists())
                .andExpect(jsonPath("$.repositories[*].forks").exists())
                .andExpect(jsonPath("$.repositories[*].createdAt").exists())
                .andExpect(jsonPath("$.repositories[*].updatedAt").exists())
                .andExpect(jsonPath("$.repositories[*].score").exists())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("repositories");
        assertThat(responseBody).contains("totalNbRepo");

        assertThat(responseBody).containsPattern("\"repositories\":\\s*\\[\\s*\\{");

    }

    @Test
    void searchWithInvalidParameters() throws Exception {
        mockMvc.perform(get("/api/repositories/search")
                        .param("language", "python")
                        .param("createdAfter", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

}