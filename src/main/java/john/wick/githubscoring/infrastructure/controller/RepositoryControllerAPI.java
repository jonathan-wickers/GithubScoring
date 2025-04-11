package john.wick.githubscoring.infrastructure.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import john.wick.githubscoring.infrastructure.controller.dto.RepositorySearchResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Repository Management", description = "APIs for GitHub repository management and scoring")
public interface RepositoryControllerAPI {

    @Operation(
            summary = "Search repositories",
            description = "Search for GitHub repositories based on language, creation date, and keywords with pagination support",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved repositories",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = RepositorySearchResultDTO.class)
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters or missing search criteria"),
                    @ApiResponse(responseCode = "500", description = "Server error")
            }
    )
    @GetMapping("/search")
    ResponseEntity<RepositorySearchResultDTO> searchRepositories(
            @Parameter(description = "Programming language filter")
            @RequestParam(required = false) @Size(max = 50)
            String language,

            @Parameter(description = "Filter repositories created after this date (YYYY-MM-DD)")
            @RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Date must be in format YYYY-MM-DD")
            String createdAfter,

            @Parameter(description = "Keyword to search in repository name and description")
            @RequestParam(required = false) @Size(max = 50)
            String keyword,

            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "1") @Min(1)
            int page,

            @Parameter(description = "Number of results per page (max 100)")
            @RequestParam(defaultValue = "20") @Max(100)
            int size,

            @Parameter(description = "Sort direction ('asc' or 'desc')")
            @RequestParam(defaultValue = "desc")
            String sortDirection);
}