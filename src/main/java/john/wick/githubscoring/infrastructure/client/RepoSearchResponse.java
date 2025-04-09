package john.wick.githubscoring.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepoSearchResponse {
    @JsonProperty("total_count")
    private int totalCount;

    @JsonProperty("incomplete_results")
    private boolean incompleteResults;

    private List<RepositoryItem> items;

    public RepoSearchResponse(int totalCount, boolean incompleteResults, List<RepositoryItem> items) {
        this.totalCount = totalCount;
        this.incompleteResults = incompleteResults;
        this.items = items;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isIncompleteResults() {
        return incompleteResults;
    }

    public void setIncompleteResults(boolean incompleteResults) {
        this.incompleteResults = incompleteResults;
    }

    public List<RepositoryItem> getItems() {
        return items;
    }

    public void setItems(List<RepositoryItem> items) {
        this.items = items;
    }

    public static class RepositoryItem {

        private Long id;
        private String name;

        @JsonProperty("stargazers_count")
        private int stargazersCount;

        @JsonProperty("forks_count")
        private int forksCount;

        @JsonProperty("updated_at")
        private LocalDate updatedAt;

        @JsonProperty("created_at")
        private LocalDate createdAt;

        private String description;

        private String language;

        public RepositoryItem(Long id, String name, int stargazersCount, int forksCount, LocalDate updatedAt, LocalDate createdAt, String description, String language) {
            this.id = id;
            this.name = name;
            this.stargazersCount = stargazersCount;
            this.forksCount = forksCount;
            this.updatedAt = updatedAt;
            this.createdAt = createdAt;
            this.description = description;
            this.language = language;
        }

        public LocalDate getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDate createdAt) {
            this.createdAt = createdAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getStargazersCount() {
            return stargazersCount;
        }

        public void setStargazersCount(int stargazersCount) {
            this.stargazersCount = stargazersCount;
        }

        public int getForksCount() {
            return forksCount;
        }

        public void setForksCount(int forksCount) {
            this.forksCount = forksCount;
        }

        public LocalDate getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDate updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;

        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
