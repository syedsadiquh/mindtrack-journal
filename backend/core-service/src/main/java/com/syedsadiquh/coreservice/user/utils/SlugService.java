package com.syedsadiquh.coreservice.user.utils;

import com.github.slugify.Slugify;
import com.syedsadiquh.coreservice.user.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlugService {

    private final TenantRepository tenantRepository;

    private static final Slugify slugify = Slugify.builder()
            .lowerCase(true)
            .locale(java.util.Locale.ENGLISH)
            .build();

    public String generateUniqueTenantSlug(String name) {
        String baseSlug = slugify.slugify(name);

        if (baseSlug.isEmpty()) {
            baseSlug = "tenant";
        }

        String finalSlug = baseSlug;
        int counter = 1;

        while (tenantRepository.existsBySlug(finalSlug)) {
            finalSlug = baseSlug + "-" + counter;
            counter++;
        }

        return finalSlug;
    }
}

