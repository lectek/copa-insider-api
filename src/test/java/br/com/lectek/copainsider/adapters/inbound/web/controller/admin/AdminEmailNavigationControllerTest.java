package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.otp.OtpServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = {
        AdminEmailController.class,
        AdminMarketingNotificacoesRedirectController.class
})
@AutoConfigureMockMvc(addFilters = false)
class AdminEmailNavigationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppSettingService appSettingService;

    @MockitoBean
    private OtpServicePort otpServicePort;

    @MockitoBean
    private ThymeleafViewResolver thymeleafViewResolver;

    @BeforeEach
    void setupViewResolver() throws Exception {
        when(thymeleafViewResolver.resolveViewName(any(), any()))
                .thenReturn(new MappingJackson2JsonView());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectLegacyAdminEmailRootToCanonicalPath() throws Exception {
        mockMvc.perform(get("/admin/email"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/marketing/emails/central"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectLegacyAdminEmailCenterToCanonicalPath() throws Exception {
        mockMvc.perform(get("/admin/email/central"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/marketing/emails/central"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRenderCanonicalAdminEmailCenterPage() throws Exception {
        mockMvc.perform(get("/admin/marketing/emails/central"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/admin/email/central"))
                .andExpect(model().attribute("active", equalTo("marketing")))
                .andExpect(model().attribute("pageTitle", equalTo("Central de E-mails")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectLegacyMarketingNotificationsPageToCanonicalPage() throws Exception {
        mockMvc.perform(get("/admin/marketing/notificacoes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/notificacoes"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRedirectLegacyMarketingNotificationsPostToCanonicalPage() throws Exception {
        mockMvc.perform(post("/admin/marketing/notificacoes"))
                .andExpect(status().isSeeOther())
                .andExpect(header().string("Location", "/admin/notificacoes"));
    }
}
