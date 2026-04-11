package com.syedsadiquh.coreservice.journal.exception;

public class JournalNotFoundException extends RuntimeException {
    public JournalNotFoundException(String message) {
        super(message);
    }
}
