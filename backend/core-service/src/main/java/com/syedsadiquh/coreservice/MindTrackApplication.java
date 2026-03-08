package com.syedsadiquh.coreservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.modulith.Modulithic;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@Modulithic(
        systemName = "MindTrack Journal",
        sharedModules = {"shared"}
)
public class MindTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(MindTrackApplication.class, args);
    }
}

