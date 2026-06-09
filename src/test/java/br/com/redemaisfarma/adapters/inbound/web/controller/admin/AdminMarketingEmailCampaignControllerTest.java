package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignLogRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignQueueRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.EmailCampaignRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.UsuarioJpaRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.application.service.MailService;
import br.com.redemaisfarma.domain.enums.StatusPedido;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.ui.ExtendedModelMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMarketingEmailCampaignControllerTest {
    @Mock
    private EmailCampaignRepository campaignRepository;

    @Mock
    private EmailCampaignQueueRepository queueRepository;

    @Mock
    private EmailCampaignLogRepository logRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioJpaRepository usuarioRepository;

    @Mock
    private MailService mailService;

    private AdminMarketingEmailCampaignController controller;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new AdminMarketingEmailCampaignController(
                campaignRepository,
                queueRepository,
                logRepository,
                clienteRepository,
                usuarioRepository,
                mailService,
                objectMapper
        );
        Mockito.lenient().when(campaignRepository.save(any())).thenAnswer(invocation -> {
            EmailCampaign campaign = invocation.getArgument(0);
            if (campaign.getId() == null) {
                campaign.setId(1L);
            }
            return campaign;
        });
        Mockito.lenient().when(queueRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.lenient().when(mailService.renderTemplate(anyString(), anyMap())).thenReturn("<html/>");
    }

    @Test
    void shouldQueueVipRecipientsWhenScheduledImmediately() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("VIP promo");
        form.setAssunto("Assunto");
        form.setTemplateKey("mail/promo");
        form.setSegmento("VIP");
        form.setEnvioImediato(true);

        UsuarioEntity vip = new UsuarioEntity();
        vip.setEmail("vip@example.com");
        vip.setNome("Vip User");
        when(usuarioRepository.findByClienteVipTrue()).thenReturn(List.of(vip));

        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail("vip@example.com");
        cliente.setNome("Cliente VIP");
        cliente.setAtivo(true);
        when(clienteRepository.findByEmailIgnoreCase("vip@example.com")).thenReturn(Optional.of(cliente));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());

        EmailCampaignQueue saved = captor.getValue();
        Assertions.assertThat(saved.getRecipientEmail()).isEqualTo("vip@example.com");
        Assertions.assertThat(saved.getRecipientName()).isEqualTo("Cliente VIP");
        Assertions.assertThat(saved.getStatus()).isEqualTo("PENDING");
        Assertions.assertThat(saved.getScheduledAt()).isNotNull();
        Map<String, Object> payload = objectMapper.readValue(saved.getPayloadJson(), new TypeReference<>() {});
        Assertions.assertThat(payload.get("segmento")).isEqualTo("VIP");
    }

    @Test
    void shouldQueueInactiveClientsWhenSegmentInactive90Days() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("Reengajamento");
        form.setAssunto("Assunto");
        form.setTemplateKey("mail/reengage");
        form.setSegmento("INATIVOS_90D");
        form.setEnvioImediato(false);
        LocalDateTime agendar = LocalDateTime.now().plusDays(1);
        form.setAgendarPara(agendar);

        ClienteEntity primeiro = buildCliente("old1@example.com", "Antigo 1", true);
        ClienteEntity segundo = buildCliente("old2@example.com", "Antigo 2", true);
        when(clienteRepository.findInativosAntesDe(any(LocalDateTime.class))).thenReturn(List.of(primeiro, segundo));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(2)).save(captor.capture());

        var expected = agendar.atZone(ZoneId.systemDefault()).toInstant();
        for (EmailCampaignQueue queue : captor.getAllValues()) {
            Assertions.assertThat(queue.getScheduledAt()).isEqualTo(expected);
            Map<String, Object> payload = objectMapper.readValue(queue.getPayloadJson(), new TypeReference<>() {});
            Assertions.assertThat(payload.get("segmento")).isEqualTo("INATIVOS_90D");
        }
    }

    @Test
    void shouldQueueOnlyActiveClientsWithEmailWhenNoSegment() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("General");
        form.setAssunto("Assunto");
        form.setTemplateKey("mail/general");
        form.setSegmento(null);
        form.setEnvioImediato(false);
        LocalDateTime agendar = LocalDateTime.now().plusHours(2);
        form.setAgendarPara(agendar);

        ClienteEntity ativo = buildCliente("active@example.com", "Ativo", true);
        ClienteEntity semEmail = buildCliente(null, "Sem email", true);
        ClienteEntity inativo = buildCliente("inactive@example.com", "Inativo", false);
        when(clienteRepository.findAll()).thenReturn(List.of(ativo, semEmail, inativo));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());
        EmailCampaignQueue saved = captor.getValue();
        Assertions.assertThat(saved.getRecipientEmail()).isEqualTo("active@example.com");
        Map<String, Object> payload = objectMapper.readValue(saved.getPayloadJson(), new TypeReference<>() {});
        Assertions.assertThat(payload.get("segmento")).isEqualTo("TODOS");
    }

    @Test
    void shouldQueueClientsByCategorySegment() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("Categoria");
        form.setAssunto("Promo categoria");
        form.setTemplateKey("mail/promo");
        form.setSegmento("CATEGORIA");
        form.setCategoria("Higiene");
        form.setEnvioImediato(true);

        ClienteEntity cliente = buildCliente("categoria@example.com", "Categoria", true);
        when(clienteRepository.findClientesByCategoriaComprada(eq("Higiene"), eq(StatusPedido.CANCELADO)))
                .thenReturn(List.of(cliente));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());
        Map<String, Object> payload = objectMapper.readValue(captor.getValue().getPayloadJson(), new TypeReference<>() {});
        Assertions.assertThat(payload.get("segmento")).isEqualTo("CATEGORIA");
    }

    @Test
    void shouldQueueClientsByRecencySegment() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("Recencia");
        form.setAssunto("Promo recencia");
        form.setTemplateKey("mail/recencia");
        form.setSegmento("RECENCIA");
        form.setRecenciaDias(15);
        form.setEnvioImediato(true);

        ClienteEntity cliente = buildCliente("recente@example.com", "Recente", true);
        when(clienteRepository.findClientesByRecencia(any(LocalDateTime.class), eq(StatusPedido.CANCELADO)))
                .thenReturn(List.of(cliente));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());
        Map<String, Object> payload = objectMapper.readValue(captor.getValue().getPayloadJson(), new TypeReference<>() {});
        Assertions.assertThat(payload.get("segmento")).isEqualTo("RECENCIA");
    }

    @Test
    void shouldQueueClientsByTicketSegment() throws Exception {
        AdminMarketingEmailCampaignController.CampanhaForm form = new AdminMarketingEmailCampaignController.CampanhaForm();
        form.setNome("Ticket");
        form.setAssunto("Promo ticket");
        form.setTemplateKey("mail/ticket");
        form.setSegmento("TICKET");
        form.setTicketMinimo(BigDecimal.valueOf(120));
        form.setEnvioImediato(true);

        ClienteEntity cliente = buildCliente("ticket@example.com", "Ticket", true);
        when(clienteRepository.findClientesByTicketMedio(eq(BigDecimal.valueOf(120)), eq(StatusPedido.CANCELADO)))
                .thenReturn(List.of(cliente));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.criar(form, attrs);

        ArgumentCaptor<EmailCampaignQueue> captor = ArgumentCaptor.forClass(EmailCampaignQueue.class);
        verify(queueRepository, times(1)).save(captor.capture());
        Map<String, Object> payload = objectMapper.readValue(captor.getValue().getPayloadJson(), new TypeReference<>() {});
        Assertions.assertThat(payload.get("segmento")).isEqualTo("TICKET");
    }

    private static ClienteEntity buildCliente(String email, String nome, boolean ativo) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setEmail(email);
        cliente.setNome(nome);
        cliente.setAtivo(ativo);
        return cliente;
    }

    @Test
    void filaExibeResumoDaFila() {
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(99L);
        queue.setRecipientEmail("fila@example.com");
        queue.setStatus("PENDING");
        queue.setPayloadJson("{\"foo\":\"bar\"}");
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(99L);
        campaign.setNome("Campanha x");
        EmailCampaignLog log = new EmailCampaignLog();
        log.setCampaignId(99L);
        log.setRecipientEmail("fila@example.com");
        log.setStatus("SENT");

        EmailCampaignQueueRepository.StatusCount statusCount = new EmailCampaignQueueRepository.StatusCount() {
            @Override
            public String getStatus() {
                return "PENDING";
            }

            @Override
            public Long getTotal() {
                return 5L;
            }
        };

        when(queueRepository.countByStatus()).thenReturn(List.of(statusCount));
        when(queueRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(queue));
        when(campaignRepository.findAllById(Collections.singleton(queue.getCampaignId()))).thenReturn(List.of(campaign));
        when(logRepository.findTop20ByOrderByCreatedAtDesc()).thenReturn(List.of(log));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.fila(model);

        Assertions.assertThat(view).isEqualTo("pages/admin/marketing/emails/fila");
        List<?> statuses = (List<?>) model.get("statuses");
        Assertions.assertThat(statuses).isNotEmpty();
        Assertions.assertThat(model.get("queueItems")).isEqualTo(List.of(queue));
        Map<?, ?> names = (Map<?, ?>) model.get("campaignNames");
        Assertions.assertThat(names.get(99L)).isEqualTo("Campanha x");
        Assertions.assertThat(model.get("logs")).isEqualTo(List.of(log));
    }

    @Test
    void previewReturnsRenderedTemplate() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(10L);
        campaign.setTemplateKey("mail/promo");
        campaign.setNome("Preview");
        campaign.setSegmentJson("{\"segmento\":\"VIP\"}");
        when(campaignRepository.findById(eq(10L))).thenReturn(Optional.of(campaign));
        when(mailService.renderTemplate(anyString(), anyMap())).thenReturn("<h1>Preview</h1>");

        String html = controller.preview(10L);

        Assertions.assertThat(html).contains("Preview");
        verify(mailService).renderTemplate(eq("mail/promo"), anyMap());
    }

    @Test
    void cancelarRemovesPendingQueueAndUpdatesStatus() {
        EmailCampaign campaign = new EmailCampaign();
        campaign.setId(11L);
        campaign.setStatus("SCHEDULED");
        when(campaignRepository.findById(eq(11L))).thenReturn(Optional.of(campaign));
        EmailCampaignQueue queueItem = new EmailCampaignQueue();
        queueItem.setCampaignId(11L);
        queueItem.setStatus("PENDING");
        when(queueRepository.findByCampaignIdAndStatusIn(eq(11L), anyList())).thenReturn(List.of(queueItem));

        RedirectAttributesModelMap attrs = new RedirectAttributesModelMap();
        controller.cancelar(11L, attrs);

        Assertions.assertThat(campaign.getStatus()).isEqualTo("CANCELLED");
        Assertions.assertThat(queueItem.getStatus()).isEqualTo("CANCELLED");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EmailCampaignQueue>> captor = ArgumentCaptor.forClass((Class<List<EmailCampaignQueue>>) (Class<?>) List.class);
        verify(queueRepository).saveAll(captor.capture());
        Assertions.assertThat(captor.getValue()).containsExactly(queueItem);
    }
}
