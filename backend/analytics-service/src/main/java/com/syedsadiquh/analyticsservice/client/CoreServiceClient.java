package com.syedsadiquh.analyticsservice.client;

import com.syedsadiquh.analyticsservice.client.dto.JournalPageDto;
import com.syedsadiquh.analyticsservice.dto.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "core-service", configuration = com.syedsadiquh.analyticsservice.config.FeignConfig.class)
public interface CoreServiceClient {

    /**
     * Lean analytics feed. Returns only fields needed for analytics computation
     * (entryDate, sentiment fields) — no blocks, tags, or enrichment.
     * JWT is forwarded automatically via FeignConfig interceptor.
     */
    @GetMapping("/api/v1/journals/pages/analytics-feed")
    BaseResponse<List<JournalPageDto>> getAnalyticsFeed(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to);
}
