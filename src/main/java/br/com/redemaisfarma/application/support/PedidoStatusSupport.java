package br.com.redemaisfarma.application.support;

import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.StatusPedido;

public final class PedidoStatusSupport {

    private PedidoStatusSupport() {
    }

    public static StatusPedido initialStatus(final boolean paymentOnDelivery) {
        return paymentOnDelivery
                ? StatusPedido.ABERTO
                : StatusPedido.AGUARDANDO_PAGAMENTO;
    }

    public static String adminLabel(
            final StatusPedido status,
            final ModoEntrega modoEntrega
    ) {
        if (status == null) {
            return "Desconhecido";
        }
        return switch (status) {
            case ABERTO -> "Aberto";
            case AGUARDANDO_PAGAMENTO -> "Aguardando pagamento";
            case PAGO -> "Pago";
            case PRONTO_PARA_ENTREGA -> "Pronto para entrega";
            case PRONTO_PARA_RETIRADA -> "Pronto para retirada";
            case SAIU_PARA_ENTREGA, ENVIADO -> isDelivery(modoEntrega)
                    ? "Saiu para entrega"
                    : "Enviado";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    public static String customerLabel(
            final StatusPedido status,
            final ModoEntrega modoEntrega
    ) {
        if (status == null) {
            return "Desconhecido";
        }
        return switch (status) {
            case ABERTO -> "Pedido recebido";
            case AGUARDANDO_PAGAMENTO -> "Aguardando pagamento";
            case PAGO -> "Pagamento confirmado";
            case PRONTO_PARA_ENTREGA -> "Pronto para entrega";
            case PRONTO_PARA_RETIRADA -> "Pronto para retirada";
            case SAIU_PARA_ENTREGA, ENVIADO -> isDelivery(modoEntrega)
                    ? "Saiu para entrega"
                    : "Enviado";
            case ENTREGUE -> "Entregue";
            case CANCELADO -> "Cancelado";
        };
    }

    public static boolean isDeliveryReady(final StatusPedido status) {
        return status == StatusPedido.PRONTO_PARA_ENTREGA
                || status == StatusPedido.PAGO;
    }

    public static boolean isDeliveryInTransit(final StatusPedido status) {
        return status == StatusPedido.SAIU_PARA_ENTREGA
                || status == StatusPedido.ENVIADO;
    }

    public static boolean isDeliveryStatusOnly(
            final StatusPedido status,
            final ModoEntrega modoEntrega
    ) {
        return isDelivery(modoEntrega)
                && (status == StatusPedido.PRONTO_PARA_ENTREGA
                || status == StatusPedido.SAIU_PARA_ENTREGA
                || status == StatusPedido.ENVIADO);
    }

    public static boolean isDelivery(final ModoEntrega modoEntrega) {
        return modoEntrega == null || modoEntrega == ModoEntrega.ENTREGA;
    }
}
