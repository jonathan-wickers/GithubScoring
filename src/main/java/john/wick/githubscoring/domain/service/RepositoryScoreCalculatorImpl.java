package john.wick.githubscoring.domain.service;

import john.wick.githubscoring.domain.port.RepositoryScoreCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class RepositoryScoreCalculatorImpl implements RepositoryScoreCalculator {

    public double calculateScore(int stars, int forks, LocalDate createdAt, LocalDate updatedAt) {

        double baseScore = Math.log10(stars + 1) * 10 + Math.log10(forks * 3 + 1) * 3;

        if (createdAt == null || updatedAt == null) {
            return baseScore;
        }

        double ageInDays = ChronoUnit.DAYS.between(createdAt, LocalDate.now());
        double ageFactor = Math.max(0, 1.3 - (ageInDays / 365 / 10));

        double daysSinceUpdate = ChronoUnit.DAYS.between(updatedAt, LocalDate.now());
        double activityFactor = 1.5 - Math.min(0.5, daysSinceUpdate / 100);

        double rawScore = baseScore * ageFactor * activityFactor;
        return Math.round(rawScore * 100) / 100.0;
    }

}
