package br.com.lectek.copainsider.application.service.delivery;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.config.AppProps;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryPricingServiceTest {

    private DeliveryRouteService deliveryRouteService;
    private AppSettingService appSettingService;
    private AppProps appProps;
    private UsuarioRepository usuarioRepository;
    private DeliveryPricingService service;

    @BeforeEach
    void setUp() {
        deliveryRouteService = mock(DeliveryRouteService.class);
        appSettingService = mock(AppSettingService.class);
        appProps = mock(AppProps.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new DeliveryPricingService(
                deliveryRouteService,
                appSettingService,
                appProps,
                usuarioRepository
        );
        when(appProps.getAddressQuery()).thenReturn("Loja Centro, 100");
    }

    @Test
    void quoteForAddressRetornaMensagemRevisadaQuandoDistanciaNaoPuderSerCalculada() {
        when(deliveryRouteService.estimateDistanceBetween(
                "Loja Centro, 100",
                "Rua Exemplo, 123"
        )).thenReturn(null);

        DeliveryQuoteVM quote = service.quoteForAddress("Rua Exemplo, 123");

        assertThat(quote.available()).isFalse();
        assertThat(quote.summary())
                .isEqualTo("Nao foi possivel calcular o frete para esse endereco.");
        assertThat(quote.detail()).isEqualTo("Revise a rua e tente novamente.");
    }

    @Test
    void quoteForAddressRetornaMensagemRevisadaQuandoServicoDeRotaFalha() {
        when(deliveryRouteService.estimateDistanceBetween(
                "Loja Centro, 100",
                "Rua Exemplo, 123"
        )).thenThrow(new IllegalStateException("falha"));

        DeliveryQuoteVM quote = service.quoteForAddress("Rua Exemplo, 123");

        assertThat(quote.available()).isFalse();
        assertThat(quote.summary())
                .isEqualTo("Nao foi possivel calcular o frete para esse endereco.");
        assertThat(quote.detail()).isEqualTo("Revise a rua e tente novamente.");
    }
}
