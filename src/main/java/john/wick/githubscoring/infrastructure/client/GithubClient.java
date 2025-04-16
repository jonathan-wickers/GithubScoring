package john.wick.githubscoring.infrastructure.client;

import john.wick.githubscoring.infrastructure.client.dto.RepoSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "githubClient",
        url = "${github.api.base-url}"
)
public interface GithubClient {

    @GetMapping("/search/repositories")
    RepoSearchResponse searchRepositories(@RequestParam("q") String query,
                                          @RequestParam("page") int page,
                                          @RequestParam("per_page") int perPage,
                                          @RequestParam("order") String order);
}