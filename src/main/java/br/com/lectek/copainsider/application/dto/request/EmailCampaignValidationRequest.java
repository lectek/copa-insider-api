package br.com.lectek.copainsider.application.dto.request;

import jakarta.validation.constraints.NotBlank;

public class EmailCampaignValidationRequest {
    @NotBlank
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
