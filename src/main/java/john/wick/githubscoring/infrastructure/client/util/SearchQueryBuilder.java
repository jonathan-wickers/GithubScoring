package john.wick.githubscoring.infrastructure.client.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SearchQueryBuilder {
    private final StringBuilder queryBuilder = new StringBuilder();

    public SearchQueryBuilder withKeyword(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            queryBuilder.append("q=").append(keyword.trim()).append(" ");
        }
        return this;
    }

    public SearchQueryBuilder withLanguage(String language) {
        if (language != null && !language.isBlank()) {
            queryBuilder.append("language:").append(language.trim()).append(" ");
        }
        return this;
    }

    public SearchQueryBuilder withCreatedAfter(LocalDate dateTime) {
        if (dateTime != null) {
            String formattedDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE);
            queryBuilder.append("created:>").append(formattedDate).append(" ");
        }
        return this;
    }


    public String build() {
        return queryBuilder.toString().trim();
    }
}

