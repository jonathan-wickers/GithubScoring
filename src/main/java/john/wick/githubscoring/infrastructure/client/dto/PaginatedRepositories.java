package john.wick.githubscoring.infrastructure.client.dto;

import john.wick.githubscoring.domain.model.Repository;

import java.util.List;

public class PaginatedRepositories {
    List<Repository> repositories;
    int currentPage;
    int totalNbPage;
    int totalNbRepo;

    public PaginatedRepositories(List<Repository> repositories, int currentPage, int totalNbPage, int totalNbRepo) {
        this.repositories = repositories;
        this.currentPage = currentPage;
        this.totalNbPage = totalNbPage;
        this.totalNbRepo = totalNbRepo;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public void setRepositories(List<Repository> repositories) {
        this.repositories = repositories;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int gettotalNbPage() {
        return totalNbPage;
    }

    public int getTotalNbRepo() {
        return totalNbRepo;
    }
}
