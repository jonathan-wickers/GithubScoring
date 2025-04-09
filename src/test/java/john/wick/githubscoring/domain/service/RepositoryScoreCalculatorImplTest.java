package john.wick.githubscoring.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryScoreCalculatorImplTest {

    private RepositoryScoreCalculatorImpl calculator;
    private LocalDate now;
    private LocalDate oneYearAgo;
    private LocalDate yesterday;

    @BeforeEach
    void setUp() {
        calculator = new RepositoryScoreCalculatorImpl();
        now = LocalDate.now();
        oneYearAgo = now.minusYears(1);
        yesterday = now.minusDays(1);
    }

    @Test
    void missingDatesOnlyUseBaseScore() {
        double score = calculator.calculateScore(100, 50, null, null);

        assertThat(score).isBetween(26.0, 27.0);
    }

    @Test
    void popularReposShouldScoreHigher() {
        double highScore = calculator.calculateScore(10000, 5000, oneYearAgo, yesterday);

        double lowScore = calculator.calculateScore(100, 50, oneYearAgo, yesterday);

        assertThat(highScore).isGreaterThan(lowScore * 1.5);
    }

    @Test
    void recentLastUpdateGivesBetterScore() {
        double trendingScore = calculator.calculateScore(1000, 500, now.minusMonths(1), yesterday);

        double staleScore = calculator.calculateScore(1000, 500, now.minusYears(5), now.minusYears(1));

        assertThat(trendingScore).isGreaterThan(staleScore);
    }

    @Test
    void edgeCaseZeroStarsAndForks() {
        double score = calculator.calculateScore(0, 0, oneYearAgo, yesterday);
        assertThat(score).isEqualTo(0.0);

        double forksOnlyScore = calculator.calculateScore(0, 100, oneYearAgo, yesterday);
        assertThat(forksOnlyScore).isPositive();
    }

    @ParameterizedTest
    @CsvSource({
            "100, 50, 26.58",
            "1000, 500, 39.54",
            "10000, 5000, 52.51"
    })
    void verifyBaseScoreAcrossPopularityLevels(int stars, int forks, double expectedBaseScore) {
        LocalDate sameAge = now.minusMonths(6);
        LocalDate sameActivity = now.minusDays(10);

        double actualScore = calculator.calculateScore(stars, forks, sameAge, sameActivity);
        double baseScore = expectedBaseScore * 1.25 * 1.4;

        assertThat(actualScore).isCloseTo(baseScore, within(3.0));
    }

    private org.assertj.core.data.Offset<Double> within(double tolerance) {
        return org.assertj.core.data.Offset.offset(tolerance);
    }
}
