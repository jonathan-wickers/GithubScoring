package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class RepositoryScoreCalculatorImpl implements RepositoryScoreCalculator {

    public double calculateScore(int stars, int forks, LocalDate createdAt, LocalDate updatedAt) {

        final double STARS_WEIGHT = 2;
        final double FORKS_WEIGHT = 1;

        if (updatedAt == null) {
            updatedAt = createdAt;
        }

        long daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());
        long repoAgeInDays = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());

        double recencyFactor = Math.exp(-daysSinceUpdate / 180.0);

        double popularityScore = (stars * STARS_WEIGHT) + (forks * FORKS_WEIGHT);

        double starsPerDay = stars / (double) (repoAgeInDays + 1);
        double normalizedPopularity = popularityScore * (1 + Math.min(1.0, starsPerDay));

        double rawScore = normalizedPopularity * (0.7 + (0.3 * recencyFactor));
        return Math.round(rawScore * 100) / 100.0;
    }

}
