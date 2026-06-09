package br.com.redemaisfarma.domain.fiscal;

public enum FiscalPrintJobEventType {
    CREATED,
    DOCUMENT_SYNC,
    HELD,
    RELEASED,
    STATION_ASSIGNED,
    CANCELLED,
    PRINTING_STARTED,
    PRINTED,
    FAILED,
    REQUEUED
}
