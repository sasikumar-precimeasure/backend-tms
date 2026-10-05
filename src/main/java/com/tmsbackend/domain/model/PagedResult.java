package com.tmsbackend.domain.model;

import java.util.List;

// A plain, framework-agnostic paging envelope for domain/application-layer
// query results - keeps Spring Data's own Page<T> (which carries a Pageable
// and other infrastructure-layer concerns) out of the domain and application
// layers, consistent with this codebase's clean-architecture boundary
// (domain/application must not import Spring Data types).
public record PagedResult<T>(List<T> items, long totalItems, int page, int pageSize) {
    public int totalPages() {
        return pageSize <= 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
    }
}
