package john.wick.githubscoring.domain.model;

import java.time.LocalDate;

public record RepoSearchCriteria(
        String language,
        LocalDate createdAfter,
        String keyword,
        int page,
        int size,
        String sortDirection

) {
    public RepoSearchCriteria(String language, LocalDate createdAfter, String keyword) {
        this(language, createdAfter, keyword, 0, 20, "desc");
    }

    public RepoSearchCriteria(String language, LocalDate createdAfter) {
        this(language, createdAfter, "", 0, 20, "desc");
    }

    public boolean hasLanguage() {
        return language != null && !language.isBlank();
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasAtLeastOneCriteria() {
        return hasLanguage() || createdAfter != null || hasKeyword();
    }

    public boolean isDateInPast() {
        return createdAfter == null || createdAfter.isBefore(LocalDate.now());
    }

}
