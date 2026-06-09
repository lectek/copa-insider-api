package br.com.redemaisfarma.adapters.outbound.persistence.jpa;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.EmailCampaign;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCampaignRepository extends JpaRepository<EmailCampaign, Long> {
    Optional<EmailCampaign> findByTemplateKey(String templateKey);
}
