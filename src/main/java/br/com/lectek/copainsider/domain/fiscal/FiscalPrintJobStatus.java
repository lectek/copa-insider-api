package br.com.lectek.copainsider.domain.fiscal;

public enum FiscalPrintJobStatus {
    WAITING_DOCUMENT,
    READY,
    HELD,
    PRINTING,
    PRINTED,
    FAILED,
    CANCELLED
}
