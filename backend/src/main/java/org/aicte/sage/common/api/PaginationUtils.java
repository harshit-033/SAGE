package org.aicte.sage.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PaginationUtils {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private PaginationUtils() {}

    public static Pageable validateAndBuildPageable(int page, int size, String sortBy, String sortDir,
                                                    Set<String> allowedSortFields, String defaultSortBy) {
        if (page < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero (received: " + page + ")");
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be at least 1 (received: " + size + ")");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot exceed maximum limit of " + MAX_PAGE_SIZE + " (requested: " + size + ")");
        }

        String effectiveSortBy = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim() : defaultSortBy;
        if (!allowedSortFields.contains(effectiveSortBy)) {
            throw new IllegalArgumentException("Invalid sort field: '" + effectiveSortBy + "'. Allowed sort fields are: " + allowedSortFields);
        }

        String effectiveSortDir = (sortDir != null && !sortDir.isBlank()) ? sortDir.trim().toLowerCase() : "asc";
        if (!effectiveSortDir.equals("asc") && !effectiveSortDir.equals("desc")) {
            throw new IllegalArgumentException("Invalid sort direction: '" + sortDir + "'. Must be 'asc' or 'desc'");
        }

        Sort sort = effectiveSortDir.equals("desc") ? Sort.by(effectiveSortBy).descending() : Sort.by(effectiveSortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
