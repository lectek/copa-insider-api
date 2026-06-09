package br.com.lectek.copainsider.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.EmailDeliveryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessException;

@ExtendWith(MockitoExtension.class)
class EmailDeliveryWorkerTest {

    @Mock
    private EmailDeliveryRepository repository;

    @Mock
    private MailService mailService;

    @Mock
    private ObjectMapper objectMapper;

    private EmailDeliveryWorker worker;

    @BeforeEach
    void setUp() {
        worker = new EmailDeliveryWorker(repository, mailService, objectMapper);
    }

    @Test
    void processBatchShouldSwallowMissingTable() {
        DataAccessException missingTable = new NonTransientDataAccessException("missing",
                new SQLException("no table", "42S02")) {};
        when(repository.findByStatusOrderByCreatedAtAsc(anyString(), any()))
                .thenThrow(missingTable);

        worker.processBatch();
        worker.processBatch(); // second call should still be safe

        verify(mailService, never()).sendTemplate(any(), any(), any(), any(), any());
    }

    @Test
    void processBatchShouldPropagateOtherSqlExceptions() {
        DataAccessException otherError = new NonTransientDataAccessException("oops",
                new SQLException("syntax", "42000")) {};
        when(repository.findByStatusOrderByCreatedAtAsc(anyString(), any()))
                .thenThrow(otherError);

        assertThatThrownBy(() -> worker.processBatch())
                .isSameAs(otherError);
    }

    @Test
    void maskDestinationHandlesVariousValues() {
        assertThat(worker.maskDestination("user@gmail.com")).isEqualTo("***@gmail.com");
        assertThat(worker.maskDestination("sem-arroba")).isEqualTo("<destino oculto>");
        assertThat(worker.maskDestination(null)).isEqualTo("<desconhecido>");
        assertThat(worker.maskDestination("")).isEqualTo("<desconhecido>");
    }
}
