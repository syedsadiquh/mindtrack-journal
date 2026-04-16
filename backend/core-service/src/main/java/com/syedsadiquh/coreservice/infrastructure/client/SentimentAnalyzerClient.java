package com.syedsadiquh.coreservice.infrastructure.client;

import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentRequest;
import com.syedsadiquh.coreservice.infrastructure.client.dto.SentimentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client targeting the Python ML service.
 * URL is configured via ${ml.sentiment-analyzer.url} in core-service.yaml.
 */
@FeignClient(
        name = "sentiment-analyzer",
        url = "${ml.sentiment-analyzer.url}"
)
public interface SentimentAnalyzerClient {

    @PostMapping("/analyze")
    SentimentResponse analyze(@RequestBody SentimentRequest request);
}

