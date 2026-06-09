package br.com.lectek.copainsider.adapters.inbound.scheduler;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EstoqueBaixoNotificacaoJob {

    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 2;
    private static final int MIN_ALERTA_ESTOQUE_LIMITE = 2;
    private static final String KEY_ENABLED = "app.estoque.alerta.enabled";
    private static final String KEY_LIMIT = "app.estoque.alerta.limite";
    private static final String KEY_COOLDOWN_MINUTES = "app.estoque.alerta.cooldown-minutes";
    private static final String KEY_LAST_RUN = "app.estoque.alerta.last-run-epoch";

    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteNotificacaoRepository notificacaoRepository;
    private final AppSettingService settings;

    public EstoqueBaixoNotificacaoJob(ProdutoRepository produtoRepository,
                                      UsuarioRepository usuarioRepository,
                                      ClienteNotificacaoRepository notificacaoRepository,
                                      AppSettingService settings) {
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.settings = settings;
    }

    @Scheduled(cron = "${app.estoque.alerta.cron:0 */30 * * * *}")
    public void executar() {
        if (!settings.getBoolean(KEY_ENABLED, true)) {
            return;
        }

        long now = Instant.now().toEpochMilli();
        long lastRun = settings.getLong(KEY_LAST_RUN, 0L);
        int cooldownMinutes = settings.getInt(KEY_COOLDOWN_MINUTES, 60);
        long cooldownMs = Math.max(0, cooldownMinutes) * 60_000L;
        if (cooldownMs > 0 && (now - lastRun) < cooldownMs) {
            return;
        }

        int limite = Math.max(
                MIN_ALERTA_ESTOQUE_LIMITE,
                settings.getInt(KEY_LIMIT, DEFAULT_ALERTA_ESTOQUE_LIMITE)
        );
        int qtdBaixo = produtoRepository.findComEstoqueBaixo(limite).size();
        if (qtdBaixo <= 0) {
            return;
        }

        List<UsuarioEntity> usuarios = usuarioRepository.findAll().stream()
                .filter(this::isAdminUser)
                .toList();
        if (usuarios.isEmpty()) {
            return;
        }

        List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        String titulo = "Estoque baixo";
        String mensagem = qtdBaixo
                + " produtos com estoque acima de 0 e abaixo de "
                + limite
                + " unidades.";
        for (UsuarioEntity usuario : usuarios) {
            ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
            notificacao.setUsuario(usuario);
            notificacao.setTipo("ESTOQUE");
            notificacao.setTitulo(titulo);
            notificacao.setMensagem(mensagem);
            criadas.add(notificacao);
        }
        notificacaoRepository.saveAll(criadas);
        settings.upsert(KEY_LAST_RUN, String.valueOf(now), "Timestamp ultimo alerta de estoque");
    }

    private boolean isAdminUser(UsuarioEntity usuario) {
        if (usuario == null) {
            return false;
        }
        Set<String> roles = usuario.getRoleNames();
        return roles.contains("ADMIN") || roles.contains("ROLE_ADMIN");
    }
}
