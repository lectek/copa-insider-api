/*
 * Decompiled with CFR 0.152.
 */
package br.com.lectek.copainsider.application.core.exception;

public class PedidoNaoEncontradoException
extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "O pedido solicitado n\u00e3o foi encontrado.";
    private static final String DEFAULT_CODE = "PEDIDO_NAO_ENCONTRADO";
    private final String codigoErro;

    public PedidoNaoEncontradoException() {
        super(DEFAULT_MESSAGE);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(String message) {
        super(message != null ? message : DEFAULT_MESSAGE);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(String message, Throwable cause) {
        super(message != null ? message : DEFAULT_MESSAGE, cause);
        this.codigoErro = DEFAULT_CODE;
    }

    public PedidoNaoEncontradoException(Throwable cause) {
        super(DEFAULT_MESSAGE, cause);
        this.codigoErro = DEFAULT_CODE;
    }

    public String getCodigoErro() {
        return this.codigoErro;
    }
}

