package br.com.redemaisfarma.domain.fiscal;

public enum FiscalPrintJobStatus {
    WAITING_DOCUMENT,
    READY,
    HELD,
    PRINTING,
    PRINTED,
    FAILED,
    CANCELLED
}
