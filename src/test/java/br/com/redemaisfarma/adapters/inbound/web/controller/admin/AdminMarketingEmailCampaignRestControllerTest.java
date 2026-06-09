package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.application.dto.request.EmailCampaignRequest;
import br.com.redemaisfarma.application.dto.request.EmailCampaignValidationRequest;
import br.com.redemaisfarma.application.dto.response.CampaignQueuePreviewResponse;
import br.com.redemaisfarma.application.dto.response.EmailCampaignResponse;
import br.com.redemaisfarma.application.dto.response.QueueDashboardResponse;
import br.com.redemaisfarma.application.dto.response.QueueStatusResponse;
import br.com.redemaisfarma.application.service.EmailCampaignService;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignQueue;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMarketingEmailCampaignRestControllerTest {
    @Mock
    private EmailCampaignService campaignService;

    private AdminMarketingEmailCampaignRestController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminMarketingEmailCampaignRestController(campaignService);
    }

    @Test
    void listReturnsPage() {
        EmailCampaignResponse response = new EmailCampaignResponse();
        response.setId(1L);
        when(campaignService.list(any())).thenReturn(new PageImpl<>(List.of(response)));

        Page<EmailCampaignResponse> page = controller.list(PageRequest.of(0, 10));

        Assertions.assertThat(page.getContent()).containsExactly(response);
    }

    @Test
    void previewReturnsHtml() {
        when(campaignService.preview(5L)).thenReturn("<html/>");

        Assertions.assertThat(controller.preview(5L).getBody()).isEqualTo("<html/>");
    }

    @Test
    void queueBuildsDashboard() {
        QueueStatusResponse status = new QueueStatusResponse("PENDING", 3L);
        EmailCampaignQueue queue = new EmailCampaignQueue();
        queue.setCampaignId(10L);
        queue.setRecipientEmail("user@example.com");
        queue.setStatus("PENDING");
        queue.setAttempts(1);
        queue.setScheduledAt(Instant.now());
        EmailCampaignLog log = new EmailCampaignLog();
        log.setCampaignId(10L);
        log.setRecipientEmail("user@example.com");
        log.setStatus("SENT");

        when(campaignService.queueStatus()).thenReturn(List.of(status));
        when(campaignService.latestQueueItems()).thenReturn(List.of(queue));
        when(campaignService.latestLogs()).thenReturn(List.of(log));

        QueueDashboardResponse dashboard = controller.queue();

        Assertions.assertThat(dashboard.getStatuses()).hasSize(1);
        Assertions.assertThat(dashboard.getQueueItems()).hasSize(1);
        Assertions.assertThat(dashboard.getLogs()).hasSize(1);
    }

    @Test
    void createDelegatesToService() {
        EmailCampaignRequest request = new EmailCampaignRequest();
        EmailCampaignResponse response = new EmailCampaignResponse();
        response.setId(99L);
        when(campaignService.create(any())).thenReturn(response);

        Assertions.assertThat(controller.create(request).getBody()).isEqualTo(response);
    }

    @Test
    void pauseDelegatesToService() {
        EmailCampaignResponse response = new EmailCampaignResponse();
        when(campaignService.pause(7L)).thenReturn(response);

        Assertions.assertThat(controller.pause(7L).getBody()).isEqualTo(response);
    }

    @Test
    void resumeDelegatesToService() {
        EmailCampaignResponse response = new EmailCampaignResponse();
        when(campaignService.resume(8L)).thenReturn(response);

        Assertions.assertThat(controller.resume(8L).getBody()).isEqualTo(response);
    }

    @Test
    void validateDelegatesToService() {
        EmailCampaignResponse response = new EmailCampaignResponse();
        EmailCampaignValidationRequest request = new EmailCampaignValidationRequest();
        request.setStatus("approved");
        when(campaignService.updateValidationStatus(eq(12L), any(EmailCampaignValidationRequest.class)))
                .thenReturn(response);

        Assertions.assertThat(controller.validate(12L, request).getBody()).isEqualTo(response);
    }

    @Test
    void previewMetricsDelegatesToService() {
        CampaignQueuePreviewResponse response = new CampaignQueuePreviewResponse();
        when(campaignService.queuePreviewMetrics(eq(20L), any(), any(), any(), any())).thenReturn(response);

        Assertions.assertThat(controller.previewMetrics(20L, List.of("PENDING"), null, null, "vip")).isEqualTo(response);
    }
}
