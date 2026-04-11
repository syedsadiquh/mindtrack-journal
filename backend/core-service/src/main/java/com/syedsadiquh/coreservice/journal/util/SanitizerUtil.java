package com.syedsadiquh.coreservice.journal.util;

import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class SanitizerUtil {
    private final PolicyFactory htmlSanitizerPolicy;

    public Map<String, Object> sanitizeMap(Map<String, Object> rawMap) {
        if (rawMap == null || rawMap.isEmpty()) {
            return rawMap;
        }

        Map<String, Object> safeMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
            Object value = entry.getValue();

            if (value instanceof String) {
                // If it's a string, sanitize it!
                String safeString = htmlSanitizerPolicy.sanitize((String) value);
                safeMap.put(entry.getKey(), safeString);
            } else if (value instanceof List) {
                // If it's a list, we need to check if it's a list of strings or a list of maps
                List<?> rawList = (List<?>) value;

                if (!rawList.isEmpty()) {
                    Object firstElement = rawList.getFirst();

                    if (firstElement instanceof String) {
                        // It's a list of strings, sanitize each string
                        List<String> safeList = rawList.stream()
                                .filter(String.class::isInstance)
                                .map(String.class::cast)
                                .map(htmlSanitizerPolicy::sanitize)
                                .toList();
                        safeMap.put(entry.getKey(), safeList);

                    } else if (firstElement instanceof Map) {
                        // It's a list of maps, sanitize each map
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> rawMapList = (List<Map<String, Object>>) rawList;
                        List<Map<String, Object>> safeMapList = rawMapList.stream()
                                .filter(Objects::nonNull)
                                .map(this::sanitizeMap)
                                .toList();
                        safeMap.put(entry.getKey(), safeMapList);
                    }
                } else {
                    // if the list is empty, put an empty list in the safe map to avoid data loss downstream
                    safeMap.put(entry.getKey(), new ArrayList<>());
                }

            } else if (value instanceof Map) {
                // If it's a nested map, recursively sanitize it
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                safeMap.put(entry.getKey(), sanitizeMap(nestedMap));

            } else {
                // If it's an Integer, Boolean, List, etc., just pass it through safely
                safeMap.put(entry.getKey(), value);
            }
        }

        return safeMap;
    }
}
