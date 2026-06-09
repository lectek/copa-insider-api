package br.com.lectek.copainsider.application.core.account;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.domain.user.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private ClienteRepository clientes;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private RoleRepository roles;

    @InjectMocks
    private UserAccountService service;

    @BeforeEach
    void setUp() {
        when(encoder.encode(anyString())).thenReturn("encoded");
        when(roles.findByNome("ROLE_USER")).thenReturn(Optional.of(Role.of("ROLE_USER")));
    }

    @Test
    void registerUsesEmailAndGeneratesDefaults() {
        when(usuarios.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarios.existsByCpf(anyString())).thenReturn(false);
        when(usuarios.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientes.findByEmailIgnoreCase(anyString())).thenReturn(java.util.Optional.empty());
        when(clientes.findByCpf(anyString())).thenReturn(java.util.Optional.empty());

        service.register(null, "User@Example.com", null, "senhaSegura123");

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarios).save(captor.capture());
        verify(clientes).save(org.mockito.ArgumentMatchers.any());
        UsuarioEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getNome()).isEqualTo("user");
        assertThat(saved.getCpf()).hasSize(11);
        assertThat(saved.getRoles()).isEqualTo(Set.of(Role.of("ROLE_USER")));
    }

    @Test
    void registerCreatesRoleWhenDefaultDoesNotExist() {
        when(roles.findByNome("ROLE_USER")).thenReturn(Optional.empty());
        when(roles.findByNome("USER")).thenReturn(Optional.empty());
        when(roles.save(any(Role.class))).thenReturn(Role.of("ROLE_USER"));
        when(usuarios.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarios.existsByCpf(anyString())).thenReturn(false);
        when(usuarios.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clientes.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());
        when(clientes.findByCpf(anyString())).thenReturn(Optional.empty());

        service.register("Cliente", "novo@example.com", "12345678901", "senhaSegura123");

        verify(roles).save(any(Role.class));
    }
}
