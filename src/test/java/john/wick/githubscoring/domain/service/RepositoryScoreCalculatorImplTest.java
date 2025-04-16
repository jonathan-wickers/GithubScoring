package john.wick.githubscoring.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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

    @ParameterizedTest(name = "{index}: {4}")
    @MethodSource("scoreTestCases")
    void calculateScoreParameterized(int stars, int forks, LocalDate createdAt,
                                     LocalDate updatedAt, String scenario, double expectedScore) {
        double actualScore = calculator.calculateScore(stars, forks, createdAt, updatedAt);

        assertThat(actualScore).isCloseTo(expectedScore, within(0.01));
    }

    private static Stream<Arguments> scoreTestCases() {
        LocalDate now = LocalDate.now();
        LocalDate oneYearAgo = now.minusYears(1);
        LocalDate sixMonthsAgo = now.minusMonths(6);
        LocalDate oneMonthAgo = now.minusMonths(1);
        LocalDate oneWeekAgo = now.minusWeeks(1);

        return Stream.of(
                Arguments.of(1000, 500, oneYearAgo, oneWeekAgo, "Popular repo with recent updates", 4942.79),
                Arguments.of(1000, 500, oneYearAgo, sixMonthsAgo, "Popular repo with older updates", 4045.72),
                Arguments.of(10, 5, oneMonthAgo, oneWeekAgo, "New repo with few stars/forks", 32.44),
                Arguments.of(500, 200, sixMonthsAgo, null, "Repo with null updateDate", 1941.95),
                Arguments.of(0, 0, oneYearAgo, oneMonthAgo, "Repo with zero stars and forks", 0.0),
                Arguments.of(0, 100, oneYearAgo, oneMonthAgo, "Repo with only forks", 95.25),
                Arguments.of(10000, 3000, oneYearAgo, oneMonthAgo, "Very popular repository", 43816.73)
        );

    }
}
