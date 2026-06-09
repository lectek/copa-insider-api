package br.com.lectek.copainsider.domain.fiscal;

public enum FiscalEventType {
    DRAFT_CREATED,
    SUBMISSION_REQUESTED,
    STATUS_SYNC,
    AUTHORIZATION,
    CANCELLATION,
    WEBHOOK_RECEIVED,
    ERROR
}
