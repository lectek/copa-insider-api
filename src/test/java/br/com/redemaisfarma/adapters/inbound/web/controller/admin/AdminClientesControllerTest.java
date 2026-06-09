package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminClientesControllerTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SessionRegistry sessionRegistry;

    private AdminClientesController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminClientesController(
                clienteRepository,
                usuarioRepository,
                sessionRegistry
        );
    }

    @Test
    void listaUsaUsuarioOnlineComoFallbackQuandoNaoHaClientes() {
        ExtendedModelMap model = new ExtendedModelMap();
        UserDetails principal = User.withUsername("cliente@teste.com")
                .password("secret")
                .roles("ADMIN")
                .build();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(7L);
        usuario.setNome("Cliente Online");
        usuario.setEmail("cliente@teste.com");
        usuario.setCpf("12345678900");
        usuario.setTelefone("83999990000");
        usuario.setTentativasFalhas(0);

        when(clienteRepository.searchAdmin(
                nullable(String.class),
                nullable(Boolean.class),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(
                List.of(),
                PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "id")),
                0
        ));
        when(clienteRepository.count()).thenReturn(0L);
        when(clienteRepository.countByAtivoTrue()).thenReturn(0L);
        when(usuarioRepository.search(nullable(String.class), anyString())).thenReturn(List.of());
        when(usuarioRepository.findByEmailOrCpf("cliente@teste.com")).thenReturn(Optional.of(usuario));
        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(principal));
        when(sessionRegistry.getAllSessions(eq(principal), anyBoolean()))
                .thenReturn(List.of(new SessionInformation(principal, "sess-1", new Date())));

        String view = controller.lista(null, null, 0, 50, model);

        assertThat(view).isEqualTo("pages/admin/clientes/lista");
        assertThat(model.get("totalClientes")).isEqualTo(1L);
        assertThat(model.get("totalClientesOnline")).isEqualTo(1L);
        assertThat(model.get("totalFiltrados")).isEqualTo(1L);

        @SuppressWarnings("unchecked")
        List<ClienteEntity> clientes = (List<ClienteEntity>) model.get("clientes");
        assertThat(clientes).hasSize(1);
        assertThat(clientes.getFirst().getEmail()).isEqualTo("cliente@teste.com");

        @SuppressWarnings("unchecked")
        Map<Long, Boolean> onlineClienteMap = (Map<Long, Boolean>) model.get("onlineClienteMap");
        assertThat(onlineClienteMap).containsEntry(7L, true);
    }
}
