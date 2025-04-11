package john.wick.githubscoring.infrastructure.client.dto;

import john.wick.githubscoring.domain.model.Repository;

import java.util.List;

public class PaginatedRepositories {
    List<Repository> repositories;
    int currentPage;
    int TotalNbPage;
    int totalNbRepo;

    public PaginatedRepositories(List<Repository> repositories, int currentPage, int TotalNbPage, int totalNbRepo) {
        this.repositories = repositories;
        this.currentPage = currentPage;
        this.TotalNbPage = TotalNbPage;
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

    public int getTotalNbPage() {
        return TotalNbPage;
    }

    public int getTotalNbRepo() {
        return totalNbRepo;
    }
}
