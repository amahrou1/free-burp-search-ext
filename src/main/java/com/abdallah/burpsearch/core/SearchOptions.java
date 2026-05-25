package com.abdallah.burpsearch.core;

import java.util.Set;

public record SearchOptions(
        boolean regex,
        boolean caseSensitive,
        boolean negativeMatch,
        boolean inScopeOnly,
        Set<SearchLocation> locations,
        Set<SearchSource> sources
) {
    public SearchOptions {
        locations = Set.copyOf(locations);
        sources = Set.copyOf(sources);
    }
}
