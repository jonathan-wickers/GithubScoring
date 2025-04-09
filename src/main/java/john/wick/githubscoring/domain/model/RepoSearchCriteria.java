package john.wick.githubscoring.domain.model;

import java.time.LocalDate;

public record RepoSearchCriteria(
        String language,
        LocalDate createdAfter,
        String keyword
) {
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
