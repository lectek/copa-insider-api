package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.DoacaoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.DoacaoJPARepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class DoacaoService {

    private final DoacaoJPARepository repo;
    private final boolean stripeEnabled;

    public DoacaoService(DoacaoJPARepository repo,
                         @Value("${stripe.secret-key:}") String secretKey) {
        this.repo = repo;
        this.stripeEnabled = secretKey != null && !secretKey.isBlank() && secretKey.startsWith("sk_");
        if (stripeEnabled) Stripe.apiKey = secretKey;
    }

    public boolean isStripeEnabled() {
        return stripeEnabled;
    }

    public String criarCheckoutSession(String nome, String email, BigDecimal valor,
                                       String mensagem, String baseUrl) throws StripeException {
        DoacaoEntity d = new DoacaoEntity();
        d.setDoadorNome(nome);
        d.setDoadorEmail(email);
        d.setValor(valor);
        d.setMensagem(mensagem);

        if (!stripeEnabled) {
            d.setStatus("PENDENTE_SEM_STRIPE");
            repo.save(d);
            return null;
        }

        long centavos = valor.multiply(BigDecimal.valueOf(100)).longValue();
        Session session = Session.create(SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/doacao/obrigado?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/doacao")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("eur")
                    .setUnitAmount(centavos)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Donativo — Copa Insider")
                        .setDescription("Obrigado por apoiar o projeto!")
                        .build())
                    .build())
                .build())
            .putMetadata("doador_nome", nome != null ? nome : "")
            .putMetadata("doador_email", email != null ? email : "")
            .putMetadata("mensagem", mensagem != null ? mensagem.substring(0, Math.min(mensagem.length(), 500)) : "")
            .build());

        d.setStripeSessionId(session.getId());
        repo.save(d);
        return session.getUrl();
    }

    public void confirmarPagamento(String stripeSessionId) {
        repo.findByStripeSessionId(stripeSessionId).ifPresent(d -> {
            d.setStatus("CONFIRMADO");
            repo.save(d);
        });
    }
}
