package com.syedsadiquh.userservice.service;

import com.github.slugify.Slugify;
import com.syedsadiquh.userservice.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlugService {

    private final TenantRepository tenantRepository;

    private static final Slugify slugify = Slugify.builder()
            .lowerCase(true)
            .locale(java.util.Locale.ENGLISH) // Enforce English for consistency
            .build();

    public String generateUniqueTenantSlug(String name) {
        // 1. Generate base slug
        String baseSlug = slugify.slugify(name);

        // Edge case: If name was "!!!", slug might be empty. Fallback to "tenant"
        if (baseSlug.isEmpty()) {
            baseSlug = "tenant";
        }

        String finalSlug = baseSlug;
        int counter = 1;

        // 2. Loop until we find a unique slot
        // slug' is Indexed Unique in DB
        while (tenantRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }
}
