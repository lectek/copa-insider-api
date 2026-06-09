package br.com.lectek.copainsider.adapters.inbound.scheduler;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.domain.user.Role;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ProdutoEntity;
import java.util.List;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueBaixoNotificacaoJobTest {

    private static final String KEY_ENABLED = "app.estoque.alerta.enabled";
    private static final String KEY_LIMIT = "app.estoque.alerta.limite";
    private static final String KEY_COOLDOWN = "app.estoque.alerta.cooldown-minutes";
    private static final String KEY_LAST_RUN = "app.estoque.alerta.last-run-epoch";

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteNotificacaoRepository notificacaoRepository;

    @Mock
    private AppSettingService settings;

    private EstoqueBaixoNotificacaoJob job;

    @BeforeEach
    void setUp() {
        job = new EstoqueBaixoNotificacaoJob(produtoRepository, usuarioRepository, notificacaoRepository, settings);
    }

    @Test
    void shouldSkipWhenAlertDisabled() {
        when(settings.getBoolean(KEY_ENABLED, true)).thenReturn(false);

        job.executar();

        verify(produtoRepository, never()).findComEstoqueBaixo(any());
        verify(notificacaoRepository, never()).saveAll(any());
    }

    @Test
    void shouldSkipWhenCooldownActive() {
        when(settings.getBoolean(KEY_ENABLED, true)).thenReturn(true);
        when(settings.getLong(KEY_LAST_RUN, 0L)).thenReturn(System.currentTimeMillis());
        when(settings.getInt(KEY_COOLDOWN, 60)).thenReturn(1);

        job.executar();

        verify(produtoRepository, never()).findComEstoqueBaixo(any());
        verify(notificacaoRepository, never()).saveAll(any());
    }

    @Test
    void shouldCreateNotificationsWhenLowStock() {
        when(settings.getBoolean(KEY_ENABLED, true)).thenReturn(true);
        when(settings.getLong(KEY_LAST_RUN, 0L)).thenReturn(0L);
        when(settings.getInt(KEY_COOLDOWN, 60)).thenReturn(0);
        when(settings.getInt(KEY_LIMIT, 2)).thenReturn(2);
        when(produtoRepository.findComEstoqueBaixo(2)).thenReturn(List.of(new ProdutoEntity()));

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("user@example.com");
        usuario.setRoles(Set.of(Role.of("ADMIN")));
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        job.executar();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ClienteNotificacaoEntity>> captor = ArgumentCaptor.forClass((Class<List<ClienteNotificacaoEntity>>) (Class<?>) List.class);
        verify(notificacaoRepository).saveAll(captor.capture());
        List<ClienteNotificacaoEntity> saved = captor.getValue();
        Assertions.assertThat(saved).hasSize(1);
        Assertions.assertThat(saved.get(0).getTipo()).isEqualTo("ESTOQUE");
        Assertions.assertThat(saved.get(0).getUsuario()).isSameAs(usuario);
        verify(settings).upsert(eq(KEY_LAST_RUN), anyString(), anyString());
    }

    @Test
    void shouldSkipWhenNoUsers() {
        when(settings.getBoolean(KEY_ENABLED, true)).thenReturn(true);
        when(settings.getLong(KEY_LAST_RUN, 0L)).thenReturn(0L);
        when(settings.getInt(KEY_COOLDOWN, 60)).thenReturn(0);
        when(settings.getInt(KEY_LIMIT, 2)).thenReturn(2);
        when(produtoRepository.findComEstoqueBaixo(2)).thenReturn(List.of(new ProdutoEntity()));
        when(usuarioRepository.findAll()).thenReturn(List.of());

        job.executar();

        verify(notificacaoRepository, never()).saveAll(any());
        verify(settings, never()).upsert(eq(KEY_LAST_RUN), anyString(), anyString());
    }
}
