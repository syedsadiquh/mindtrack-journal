package com.syedsadiquh.coreservice.journal.util;

import lombok.RequiredArgsConstructor;
import org.owasp.html.PolicyFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

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
