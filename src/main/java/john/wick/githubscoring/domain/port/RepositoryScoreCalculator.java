package john.wick.githubscoring.domain.port;

import java.time.LocalDate;

public interface RepositoryScoreCalculator {
    double calculateScore(int stars, int forks, LocalDate updatedAt, LocalDate createdAt);
}
