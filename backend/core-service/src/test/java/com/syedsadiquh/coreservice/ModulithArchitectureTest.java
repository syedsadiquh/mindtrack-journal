package com.syedsadiquh.coreservice;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Validates the modular structure of the MindTrack Citadel architecture.
 * Ensures module boundaries are respected and no illegal cross-module dependencies exist.
 */
class ModulithArchitectureTest {

    ApplicationModules modules = ApplicationModules.of(MindTrackApplication.class);

    @Test
    void verifyModularStructure() {
        // This will fail the build if any module violates its declared boundaries
        modules.verify();
    }

    @Test
    void createModuleDocumentation() {
        new Documenter(modules).writeDocumentation();
    }
}

