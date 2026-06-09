package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import br.com.redemaisfarma.application.view.PaymentMethodVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    @Mock
    private AppSettingService settings;

    private PaymentMethodService service;
    private String customMethodsJson;

    @BeforeEach
    void setUp() {
        service = new PaymentMethodService(settings, new ObjectMapper());
        customMethodsJson = "[]";

        lenient().when(settings.getBoolean(anyString(), anyBoolean()))
                .thenReturn(false);
        when(settings.getOrDefault("pg.custom_methods", "[]"))
                .thenAnswer(invocation -> customMethodsJson);
    }

    @Test
    void shouldNotExposePosMethodOnCustomerCheckout() {
        customMethodsJson = """
                [
                  {
                    "id": "pos-1",
                    "nome": "Maquineta Loja",
                    "tipo": "pos",
                    "taxa": 0,
                    "ativo": true
                  },
                  {
                    "id": "custom-1",
                    "nome": "Convenio Empresa",
                    "tipo": "custom",
                    "taxa": 1.5,
                    "ativo": true
                  }
                ]
                """;

        List<PaymentMethodVM> methods = service.listActiveMethods();

        assertThat(methods)
                .extracting(PaymentMethodVM::value)
                .contains("custom:custom-1")
                .doesNotContain("custom:pos-1");
    }

    @Test
    void shouldDefaultBlankCustomTypeToCustom() {
        customMethodsJson = """
                [
                  {
                    "id": "m-1",
                    "nome": "Pagamento interno",
                    "tipo": "",
                    "taxa": null,
                    "ativo": true
                  }
                ]
                """;

        List<PaymentMethodVM> methods = service.listActiveMethods();

        assertThat(methods).hasSize(1);
        assertThat(methods.getFirst().tipo()).isEqualTo("custom");
    }

    @Test
    void shouldAppendFeeOnLabel() {
        customMethodsJson = """
                [
                  {
                    "id": "m-2",
                    "nome": "Carteira parceira",
                    "tipo": "online",
                    "taxa": 2.49,
                    "ativo": true
                  }
                ]
                """;

        List<PaymentMethodVM> methods = service.listActiveMethods();

        assertThat(methods).hasSize(1);
        assertThat(methods.getFirst().label()).contains("2.49%");
        assertThat(methods.getFirst().tipo()).isEqualTo("online");
    }

    @Test
    void shouldExposeCashByDefaultWhenSettingIsMissing() {
        when(settings.getBoolean(anyString(), anyBoolean()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        List<PaymentMethodVM> methods = service.listActiveMethods();

        assertThat(methods)
                .extracting(PaymentMethodVM::value)
                .contains("pix", "credito", "debito", "dinheiro");
        assertThat(methods)
                .filteredOn(method -> "dinheiro".equals(method.value()))
                .extracting(PaymentMethodVM::label)
                .containsExactly("Dinheiro");
    }
}
