/**
 * Journal module — bounded context for journal entries and sentiment analysis.
 * Depends on the user module's public API ({@code user::api}) for
 * cross-module calls or the data comes via the JWT.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "infrastructure", "user::api"}
)
package com.syedsadiquh.coreservice.journal;

