/**
 * Journal module — bounded context for journal entries and sentiment analysis.
 * Communicates with the user module only via userId from the JWT security context.
 */
@org.springframework.modulith.ApplicationModule(
        allowedDependencies = {"shared", "infrastructure"}
)
package com.syedsadiquh.coreservice.journal;

