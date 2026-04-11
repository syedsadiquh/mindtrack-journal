package com.syedsadiquh.coreservice.journal.config;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SanitizerConfig {

    @Bean
    public PolicyFactory htmlSanitizerPolicy() {
        return Sanitizers.FORMATTING
                .and(Sanitizers.LINKS)
                .and(Sanitizers.BLOCKS);
    }
}
