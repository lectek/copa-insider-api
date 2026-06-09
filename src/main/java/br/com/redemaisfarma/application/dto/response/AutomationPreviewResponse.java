package br.com.redemaisfarma.application.dto.response;

public class AutomationPreviewResponse {
    private Long campaignId;
    private String name;
    private String templateKey;
    private String automationType;
    private CampaignQueuePreviewResponse metrics;

    public AutomationPreviewResponse() {
    }

    public AutomationPreviewResponse(Long campaignId, String name, String templateKey, String automationType, CampaignQueuePreviewResponse metrics) {
        this.campaignId = campaignId;
        this.name = name;
        this.templateKey = templateKey;
        this.automationType = automationType;
        this.metrics = metrics;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public void setTemplateKey(String templateKey) {
        this.templateKey = templateKey;
    }

    public String getAutomationType() {
        return automationType;
    }

    public void setAutomationType(String automationType) {
        this.automationType = automationType;
    }

    public CampaignQueuePreviewResponse getMetrics() {
        return metrics;
    }

    public void setMetrics(CampaignQueuePreviewResponse metrics) {
        this.metrics = metrics;
    }
}
