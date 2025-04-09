package john.wick.githubscoring.domain.model;

import java.time.LocalDate;

public class Repository {
    private String name;
    private String description;
    private String language;
    private int stars;
    private int forks;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private double score;

    public Repository() {
    }

    public Repository(String name, String description, String language, int stars, int forks, LocalDate createdAt, LocalDate updatedAt) {
        this.name = name;
        this.description = description;
        this.language = language;
        this.stars = stars;
        this.forks = forks;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public int getForks() {
        return forks;
    }

    public void setForks(int forks) {
        this.forks = forks;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


}

