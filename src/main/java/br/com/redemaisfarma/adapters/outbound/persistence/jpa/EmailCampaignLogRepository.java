package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaignLog;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCampaignLogRepository extends JpaRepository<EmailCampaignLog, Long> {
    List<EmailCampaignLog> findByCampaignIdOrderBySentAtDesc(Long campaignId, Pageable pageable);

    List<EmailCampaignLog> findTop20ByOrderByCreatedAtDesc();
}
