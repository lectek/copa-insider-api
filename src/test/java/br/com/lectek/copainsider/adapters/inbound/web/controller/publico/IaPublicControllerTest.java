package br.com.lectek.copainsider.adapters.inbound.web.controller.publico;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.ai.AiAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IaPublicController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("dev")
class IaPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiAssistantService aiAssistantService;

    @MockitoBean
    private AppSettingService appSettingService;

    @Test
    void askShouldAcceptJsonPayloadAndReturnAnswer() throws Exception {
        when(aiAssistantService.answer(eq("sess-alysson"), eq("Qual o seu nome?")))
                .thenReturn("Meu nome e Alysson.");

        mockMvc.perform(post("/api/ia/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "message", "Qual o seu nome?",
                                "sessionId", "sess-alysson"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-alysson"))
                .andExpect(jsonPath("$.answer").value("Meu nome e Alysson."));

        verify(aiAssistantService).answer("sess-alysson", "Qual o seu nome?");
    }

    @Test
    void askShouldRejectBlankMessage() throws Exception {
        mockMvc.perform(post("/api/ia/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "message", "   "
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("message is required"));
    }

    @Test
    void askShouldReturnServiceUnavailableWhenOpenAiIsNotReady() throws Exception {
        when(aiAssistantService.answer(eq("sess-alysson"), eq("Qual o seu nome?")))
                .thenThrow(new IllegalStateException("OpenAI nao configurado para o atendimento."));

        mockMvc.perform(post("/api/ia/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "message", "Qual o seu nome?",
                                "sessionId", "sess-alysson"
                        ))))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message")
                        .value(containsString("OpenAI nao configurado para o atendimento.")));
    }
}
