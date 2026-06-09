package br.com.lectek.copainsider.application.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CampaignQueuePreviewResponse {
    private long total;
    private double failureRate;
    private Map<String, Long> statusCounts;
    private List<QueueItemSummary> sample;
    private String campaignSegment;
    private String scheduledZone;

    public CampaignQueuePreviewResponse() {
    }

    public CampaignQueuePreviewResponse(long total, double failureRate, Map<String, Long> statusCounts,
                                        List<QueueItemSummary> sample, String campaignSegment, String scheduledZone) {
        this.total = total;
        this.failureRate = failureRate;
        this.statusCounts = statusCounts;
        this.sample = sample;
        this.campaignSegment = campaignSegment;
        this.scheduledZone = scheduledZone;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public double getFailureRate() {
        return failureRate;
    }

    public void setFailureRate(double failureRate) {
        this.failureRate = failureRate;
    }

    public Map<String, Long> getStatusCounts() {
        return statusCounts;
    }

    public void setStatusCounts(Map<String, Long> statusCounts) {
        this.statusCounts = statusCounts;
    }

    public List<QueueItemSummary> getSample() {
        return sample;
    }

    public void setSample(List<QueueItemSummary> sample) {
        this.sample = sample;
    }

    public String getCampaignSegment() {
        return campaignSegment;
    }

    public void setCampaignSegment(String campaignSegment) {
        this.campaignSegment = campaignSegment;
    }

    public String getScheduledZone() {
        return scheduledZone;
    }

    public void setScheduledZone(String scheduledZone) {
        this.scheduledZone = scheduledZone;
    }

    public static class QueueItemSummary {
        private String recipientEmail;
        private String status;
        private Instant scheduledAt;
        private Integer attempts;
        private String lastError;
        private String segment;

        public QueueItemSummary() {
        }

        public QueueItemSummary(String recipientEmail, String status, Instant scheduledAt, Integer attempts, String lastError, String segment) {
            this.recipientEmail = recipientEmail;
            this.status = status;
            this.scheduledAt = scheduledAt;
            this.attempts = attempts;
            this.lastError = lastError;
            this.segment = segment;
        }

        public String getRecipientEmail() {
            return recipientEmail;
        }

        public void setRecipientEmail(String recipientEmail) {
            this.recipientEmail = recipientEmail;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getScheduledAt() {
            return scheduledAt;
        }

        public void setScheduledAt(Instant scheduledAt) {
            this.scheduledAt = scheduledAt;
        }

        public Integer getAttempts() {
            return attempts;
        }

        public void setAttempts(Integer attempts) {
            this.attempts = attempts;
        }

        public String getLastError() {
            return lastError;
        }

        public void setLastError(String lastError) {
            this.lastError = lastError;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }
    }
}
