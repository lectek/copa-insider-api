package br.com.lectek.copainsider.application.dto.response;

import java.util.List;

public class QueueDashboardResponse {
    private List<QueueStatusResponse> statuses;
    private List<EmailCampaignQueueItemResponse> queueItems;
    private List<EmailCampaignLogResponse> logs;

    public QueueDashboardResponse() {
    }

    public QueueDashboardResponse(List<QueueStatusResponse> statuses,
                                  List<EmailCampaignQueueItemResponse> queueItems,
                                  List<EmailCampaignLogResponse> logs) {
        this.statuses = statuses;
        this.queueItems = queueItems;
        this.logs = logs;
    }

    public List<QueueStatusResponse> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<QueueStatusResponse> statuses) {
        this.statuses = statuses;
    }

    public List<EmailCampaignQueueItemResponse> getQueueItems() {
        return queueItems;
    }

    public void setQueueItems(List<EmailCampaignQueueItemResponse> queueItems) {
        this.queueItems = queueItems;
    }

    public List<EmailCampaignLogResponse> getLogs() {
        return logs;
    }

    public void setLogs(List<EmailCampaignLogResponse> logs) {
        this.logs = logs;
    }
}
