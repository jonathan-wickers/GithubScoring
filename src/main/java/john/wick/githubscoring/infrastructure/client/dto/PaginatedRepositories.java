package john.wick.githubscoring.infrastructure.client.dto;

import john.wick.githubscoring.domain.model.Repository;

import java.util.List;

public class PaginatedRepositories {
    List<Repository> repositories;
    int currentPage;
    int TotalNbPage;
    int totalSize;

    public PaginatedRepositories(List<Repository> repositories, int currentPage, int TotalNbPage, int totalSize) {
        this.repositories = repositories;
        this.currentPage = currentPage;
        this.TotalNbPage = TotalNbPage;
        this.totalSize = totalSize;
    }

    public List<Repository> getRepositories() {
        return repositories;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalNbPage() {
        return TotalNbPage;
    }

    public int getTotalSize() {
        return totalSize;
    }

}
