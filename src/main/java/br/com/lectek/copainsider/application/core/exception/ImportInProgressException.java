package br.com.lectek.copainsider.application.core.exception;

public class ImportInProgressException extends RuntimeException {

    private static final String DEFAULT_MESSAGE = "Ja existe uma importacao em execucao.";

    public ImportInProgressException() {
        super(DEFAULT_MESSAGE);
    }

    public ImportInProgressException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
    }

    public ImportInProgressException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
    }

    public ImportInProgressException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
    }
}
