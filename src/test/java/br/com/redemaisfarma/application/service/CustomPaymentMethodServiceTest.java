package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomPaymentMethodServiceTest {

    private static final String KEY_CUSTOM_METHODS = "pg.custom_methods";
    private static final String DESC_CUSTOM_METHODS =
            "Metodos de pagamento personalizados";

    @Mock
    private AppSettingService settings;

    private CustomPaymentMethodService service;
    private ObjectMapper objectMapper;
    private String storedJson;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new CustomPaymentMethodService(settings, objectMapper);
        storedJson = "[]";

        when(settings.getOrDefault(eq(KEY_CUSTOM_METHODS), anyString()))
                .thenAnswer(invocation -> {
                    final String defaultValue = invocation.getArgument(1);
                    return storedJson == null ? defaultValue : storedJson;
                });

        lenient().when(settings.upsert(eq(KEY_CUSTOM_METHODS), anyString(),
                eq(DESC_CUSTOM_METHODS)))
                .thenAnswer(invocation -> {
                    storedJson = invocation.getArgument(1);
                    return null;
                });
    }

    @Test
    void shouldDefaultTipoToCustomWhenBlankOnCreate() throws Exception {
        final CustomPaymentMethodService.CustomPaymentMethod created =
                service.create(new CustomPaymentMethodService
                        .CustomPaymentMethodInput(
                        "Convenio Empresa",
                        "   ",
                        BigDecimal.valueOf(2.5),
                        true
                ));

        assertThat(created.getTipo()).isEqualTo("custom");
        final List<CustomPaymentMethodService.CustomPaymentMethod> methods =
                readStoredMethods();
        assertThat(methods).hasSize(1);
        assertThat(methods.getFirst().getTipo()).isEqualTo("custom");
    }

    @Test
    void shouldRejectInvalidTipoOnCreate() {
        assertThatThrownBy(() -> service.create(
                new CustomPaymentMethodService.CustomPaymentMethodInput(
                        "Convenio Empresa",
                        "wallet",
                        null,
                        true
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo invalido");

        verify(settings, never()).upsert(
                eq(KEY_CUSTOM_METHODS),
                anyString(),
                eq(DESC_CUSTOM_METHODS)
        );
    }

    @Test
    void shouldAcceptPosTypeOnCreate() throws Exception {
        final CustomPaymentMethodService.CustomPaymentMethod created =
                service.create(new CustomPaymentMethodService
                        .CustomPaymentMethodInput(
                        "Cartao presencial",
                        "pos",
                        BigDecimal.ZERO,
                        true
                ));

        assertThat(created.getTipo()).isEqualTo("pos");
        final List<CustomPaymentMethodService.CustomPaymentMethod> methods =
                readStoredMethods();
        assertThat(methods).hasSize(1);
        assertThat(methods.getFirst().getTipo()).isEqualTo("pos");
    }

    @Test
    void shouldDefaultTipoToCustomWhenBlankOnUpdate() throws Exception {
        seedMethod("id-1", "Convenio", "online", BigDecimal.ZERO, true);

        final CustomPaymentMethodService.CustomPaymentMethod updated =
                service.update("id-1", new CustomPaymentMethodService
                        .CustomPaymentMethodInput(
                        "Convenio",
                        "",
                        BigDecimal.ZERO,
                        true
                ));

        assertThat(updated.getTipo()).isEqualTo("custom");
        assertThat(readStoredMethods().getFirst().getTipo()).isEqualTo("custom");
    }

    @Test
    void shouldRejectInvalidTipoOnUpdate() throws Exception {
        seedMethod("id-1", "Convenio", "custom", BigDecimal.ZERO, true);

        assertThatThrownBy(() -> service.update(
                "id-1",
                new CustomPaymentMethodService.CustomPaymentMethodInput(
                        "Convenio",
                        "tef",
                        BigDecimal.ZERO,
                        true
                )
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo invalido");
    }

    private List<CustomPaymentMethodService.CustomPaymentMethod> readStoredMethods()
            throws Exception {
        return objectMapper.readValue(
                storedJson,
                new TypeReference<List<CustomPaymentMethodService
                        .CustomPaymentMethod>>() {
                }
        );
    }

    private void seedMethod(
            final String id,
            final String nome,
            final String tipo,
            final BigDecimal taxa,
            final boolean ativo
    ) throws Exception {
        final CustomPaymentMethodService.CustomPaymentMethod method =
                new CustomPaymentMethodService.CustomPaymentMethod();
        method.setId(id);
        method.setNome(nome);
        method.setTipo(tipo);
        method.setTaxa(taxa);
        method.setAtivo(ativo);
        storedJson = objectMapper.writeValueAsString(List.of(method));
    }
}
