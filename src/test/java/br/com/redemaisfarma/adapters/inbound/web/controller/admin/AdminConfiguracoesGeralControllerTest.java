package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.core.settings.AppSettingService;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminConfiguracoesGeralControllerTest {

    @Mock
    private AppSettingService settings;

    private AdminConfiguracoesGeralController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminConfiguracoesGeralController(settings);
    }

    @Test
    void formAppliesLegacyFallbackAndDefaultValues() {
        Map<String, String> persisted = new HashMap<>();
        persisted.put("GERAL.nome_sistema", "Rede Mais Farma");
        persisted.put("contato.email", "contato@redemaisfarma.com");
        persisted.put("preferencias.cadastro_rapido", "false");
        persisted.put("GERAL.habilitar_assinaturas", "yes");
        persisted.put("retirada.ativa", "1");
        persisted.put("app.estoque.alerta.enabled", "on");
        persisted.put("app.estoque.alerta.limite", "invalido");
        persisted.put("app.estoque.alerta.cooldown-minutes", "75");
        when(settings.getAllByKeys(anyCollection())).thenReturn(persisted);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.form(model);

        Assertions.assertThat(view).isEqualTo("pages/admin/configuracoes/geral");
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                (AdminConfiguracoesGeralController.ConfigGeralForm) model.get("cfg");
        Assertions.assertThat(cfg).isNotNull();
        Assertions.assertThat(cfg.getNomeSistema()).isEqualTo("Rede Mais Farma");
        Assertions.assertThat(cfg.getEmail()).isEqualTo("contato@redemaisfarma.com");
        Assertions.assertThat(cfg.getHabilitarCadastrorapido()).isFalse();
        Assertions.assertThat(cfg.getHabilitarAssinaturas()).isTrue();
        Assertions.assertThat(cfg.getRetiradaAtiva()).isTrue();
        Assertions.assertThat(cfg.getFreteGratisKm()).isEqualTo("5.00");
        Assertions.assertThat(cfg.getFreteValorKmExcedente()).isEqualTo("2.00");
        Assertions.assertThat(cfg.getFretePrioritarioAcrescimo()).isEqualTo("20.00");
        Assertions.assertThat(cfg.getAlertaEstoqueAtivo()).isTrue();
        Assertions.assertThat(cfg.getAlertaEstoqueLimite()).isEqualTo("2");
        Assertions.assertThat(cfg.getAlertaEstoqueCooldown()).isEqualTo("75");
        Assertions.assertThat(cfg.getAlertaEstoqueCron()).isEqualTo("0 */30 * * * *");
    }

    @Test
    void salvarContatoPersistsOnlyContactSection() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        cfg.setEmail("contato@redemaisfarma.com");
        cfg.setTelefone("(11) 99999-1111");
        cfg.setWhatsapp("(11) 98888-7777");
        cfg.setInstagram("@redemais");
        cfg.setSiteUrl("https://redemaisfarma.com");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, "contato", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes gerais atualizadas.");
        verify(settings).upsert(
                "contato.email",
                "contato@redemaisfarma.com",
                "Email principal"
        );
        verify(settings).upsert("contato.telefone", "(11) 99999-1111", "Telefone principal");
        verify(settings).upsert("contato.whatsapp", "(11) 98888-7777", "Whatsapp");
        verify(settings).upsert("contato.instagram", "@redemais", "Instagram");
        verify(settings).upsert("contato.site_url", "https://redemaisfarma.com", "Site oficial");
        verify(settings, times(5)).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void salvarIdentidadePersistsOnlyStoreIdentity() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        cfg.setNomeSistema("RedeMais Farma");
        cfg.setNomeFantasia("RedeMais");
        cfg.setRazaoSocial("Rede Mais Farma LTDA");
        cfg.setNomeLojaSite("RedeMais Farma");
        cfg.setSloganLoja("Saude sem complicacao");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, "identidade", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        verify(settings).upsert("loja.nome", "RedeMais Farma", "Nome do sistema");
        verify(settings).upsert("loja.nome_fantasia", "RedeMais", "Nome fantasia");
        verify(settings).upsert("loja.razao_social", "Rede Mais Farma LTDA", "Razao social");
        verify(settings).upsert("loja.nome_exibicao", "RedeMais Farma", "Nome exibido no site");
        verify(settings).upsert("loja.slogan", "Saude sem complicacao", "Slogan");
        verify(settings, times(5)).upsert(anyString(), anyString(), anyString());
    }

    @Test
    void salvarWithBlankSectionPersistsAllSections() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, "   ", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        Assertions.assertThat(attrs.getFlashAttributes()).containsKey("success");
        Assertions.assertThat(attrs.getFlashAttributes().get("success"))
                .isEqualTo("Configuracoes gerais atualizadas.");
        verify(settings, times(36)).upsert(anyString(), anyString(), anyString());
        verify(settings).upsert(eq("contato.email"), eq(""), eq("Email principal"));
        verify(settings).upsert(eq("endereco.logradouro"), eq(""), eq("Endereco"));
        verify(settings).upsert(
                eq("entrega.rota.horario_saida"),
                eq(""),
                eq("Horario de saida da rota")
        );
        verify(settings).upsert(
                eq("entrega.frete.gratis_km"),
                eq(""),
                eq("Raio gratis do frete em km")
        );
        verify(settings).upsert(eq("app.estoque.alerta.enabled"), eq("false"), eq("Alerta estoque ativo"));
    }

    @Test
    void salvarEntregaPersisteConfiguracaoDeFreteDinamico() {
        AdminConfiguracoesGeralController.ConfigGeralForm cfg =
                new AdminConfiguracoesGeralController.ConfigGeralForm();
        cfg.setFreteGratisKm("5.00");
        cfg.setFreteValorKmExcedente("2.00");
        cfg.setFretePrioritarioAcrescimo("20.00");
        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();

        String redirect = controller.salvar(cfg, "entrega", attrs);

        Assertions.assertThat(redirect).isEqualTo("redirect:/admin/configuracoes/geral");
        verify(settings).upsert(
                "entrega.frete.gratis_km",
                "5.00",
                "Raio gratis do frete em km"
        );
        verify(settings).upsert(
                "entrega.frete.valor_km_excedente",
                "2.00",
                "Valor por km excedente no frete"
        );
        verify(settings).upsert(
                "entrega.frete.prioritario.acrescimo",
                "20.00",
                "Acrescimo do frete prioritario"
        );
    }
}
