package br.com.redemaisfarma.domain.fiscal;

public enum FiscalEventType {
    DRAFT_CREATED,
    SUBMISSION_REQUESTED,
    STATUS_SYNC,
    AUTHORIZATION,
    CANCELLATION,
    WEBHOOK_RECEIVED,
    ERROR
}
