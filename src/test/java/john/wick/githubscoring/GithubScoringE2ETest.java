package john.wick.githubscoring;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
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
                .andExpect(jsonPath("$.totalNbRepo").isNumber())
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
    void paginationWorkingCorrectly() throws Exception {
        MvcResult firstPageResult = mockMvc.perform(get("/api/repositories/search")
                        .param("language", "java")
                        .param("keyword", "spring")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories").isNotEmpty())
                .andExpect(jsonPath("$.repositories", hasSize(5)))
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.totalNbPages").isNumber())
                .andReturn();

        String firstPageContent = firstPageResult.getResponse().getContentAsString();
        Integer totalNbPages = JsonPath.read(firstPageContent, "$.totalNbPages");
        Integer totalRepos = JsonPath.read(firstPageContent, "$.totalNbRepo");

        MvcResult secondPageResult = mockMvc.perform(get("/api/repositories/search")
                        .param("language", "java")
                        .param("keyword", "spring")
                        .param("page", "2")
                        .param("size", "5")
                        .param("sortDirection", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repositories").isArray())
                .andExpect(jsonPath("$.repositories").isNotEmpty())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.totalNbPages").value(totalNbPages))
                .andExpect(jsonPath("$.totalNbRepo").value(totalRepos))
                .andReturn();

        String secondPageContent = secondPageResult.getResponse().getContentAsString();

        assertThat(firstPageContent).isNotEqualTo(secondPageContent);

        List<String> firstPageRepoNames = JsonPath.read(firstPageContent, "$.repositories[*].name");
        List<String> secondPageRepoNames = JsonPath.read(secondPageContent, "$.repositories[*].name");

        assertThat(firstPageRepoNames).isNotEmpty();
        assertThat(secondPageRepoNames).isNotEmpty();
        assertThat(firstPageRepoNames).doesNotContainAnyElementsOf(secondPageRepoNames);

        assertThat(totalRepos).isGreaterThanOrEqualTo(firstPageRepoNames.size() + secondPageRepoNames.size());
    }

    @Test
    void returnsBadRequestWhenNoSearchCriteriaProvided() throws Exception {
        mockMvc.perform(get("/api/repositories/search")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortDirection", "desc"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void searchWithInvalidParameters() throws Exception {
        mockMvc.perform(get("/api/repositories/search")
                        .param("language", "python")
                        .param("createdAfter", "not-a-date"))
                .andExpect(status().isBadRequest());
    }

}