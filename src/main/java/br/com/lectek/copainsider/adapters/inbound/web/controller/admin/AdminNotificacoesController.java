package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.core.tenant.TenantResolverService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/notificacoes")
@Validated
public class AdminNotificacoesController {

    /**
     * Settings key that enables low stock alerts.
     */
    private static final String KEY_ALERTA_ESTOQUE_ENABLED =
            "app.estoque.alerta.enabled";

    /**
     * Settings key for low stock threshold.
     */
    private static final String KEY_ALERTA_ESTOQUE_LIMITE =
            "app.estoque.alerta.limite";

    /**
     * Default low stock threshold.
     */
    private static final int DEFAULT_ALERTA_ESTOQUE_LIMITE = 2;

    /**
     * Minimum allowed low stock threshold.
     */
    private static final int MIN_ALERTA_ESTOQUE_LIMITE = 2;

    /**
     * Notification type for low stock messages.
     */
    private static final String TIPO_ESTOQUE = "ESTOQUE";

    /**
     * Max length for notification type.
     */
    private static final int MAX_TIPO_LEN = 40;

    /**
     * Max length for notification title.
     */
    private static final int MAX_TITULO_LEN = 120;

    /**
     * Max length for notification message.
     */
    private static final int MAX_MENSAGEM_LEN = 500;

    /**
     * Notification repository.
     */
    private final ClienteNotificacaoRepository notificacaoRepository;

    /**
     * User repository.
     */
    private final UsuarioRepository usuarioRepository;

    /**
     * Product repository.
     */
    private final ProdutoRepository produtoRepository;

    /**
     * App settings service.
     */
    private final AppSettingService appSettingService;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    /**
     * Creates controller with dependencies.
     *
     * @param notificacaoRepo notification repository
     * @param usuarioRepo user repository
     * @param produtoRepo product repository
     * @param settingsService settings service
     */
    public AdminNotificacoesController(
            final ClienteNotificacaoRepository notificacaoRepo,
            final UsuarioRepository usuarioRepo,
            final ProdutoRepository produtoRepo,
            final AppSettingService settingsService
    ) {
        this.notificacaoRepository = notificacaoRepo;
        this.usuarioRepository = usuarioRepo;
        this.produtoRepository = produtoRepo;
        this.appSettingService = settingsService;
    }

    /**
     * Renders notifications page.
     *
     * @return notifications view
     */
    @GetMapping
    public String page() {
        return "pages/admin/notificacoes";
    }

    /**
     * Sends notification to specific users.
     *
     * @param req request payload
     * @return empty 200 response
     */
    @PostMapping("/api/enviar")
    public ResponseEntity<?> enviar(
            @Valid @RequestBody final AdminNotificacaoRequest req
    ) {
        final List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        for (Long userId : req.usuarioIds()) {
            final Optional<UsuarioEntity> usuario = usuarioRepository
                    .findById(userId);
            if (usuario.isEmpty()) {
                continue;
            }
            criadas.add(createNotification(
                    usuario.get(),
                    req.tipo(),
                    req.titulo(),
                    req.mensagem()
            ));
        }
        saveAllIfAny(criadas);
        return ResponseEntity.ok().build();
    }

    /**
     * Sends notification to all users.
     *
     * @param req request payload
     * @return empty 200 response
     */
    @PostMapping("/api/enviar/todos")
    public ResponseEntity<?> enviarTodos(
            @Valid @RequestBody final AdminNotificacaoBroadcastRequest req
    ) {
        final List<UsuarioEntity> usuarios = usuarioRepository.findAll();
        final List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        for (UsuarioEntity usuario : usuarios) {
            criadas.add(createNotification(
                    usuario,
                    req.tipo(),
                    req.titulo(),
                    req.mensagem()
            ));
        }
        saveAllIfAny(criadas);
        return ResponseEntity.ok().build();
    }

    /**
     * Sends notification to one user.
     *
     * @param id user id
     * @param req request payload
     * @return 404 when user is not found, 200 otherwise
     */
    @PostMapping("/api/enviar/usuario/{id}")
    public ResponseEntity<?> enviarParaUsuario(
            @PathVariable("id") final Long id,
            @Valid @RequestBody final AdminNotificacaoSingleRequest req
    ) {
        final Optional<UsuarioEntity> usuario = usuarioRepository.findById(id);
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final ClienteNotificacaoEntity notificacao = createNotification(
                usuario.get(),
                req.tipo(),
                req.titulo(),
                req.mensagem()
        );
        notificacaoRepository.save(notificacao);
        return ResponseEntity.ok().build();
    }

    /**
     * Sends low stock notification to admin users.
     *
     * @return 204 when there is nothing to notify, 200 otherwise
     */
    @PostMapping("/api/enviar/estoque-baixo")
    public ResponseEntity<?> enviarEstoqueBaixo() {
        if (!appSettingService.getBoolean(KEY_ALERTA_ESTOQUE_ENABLED, true)) {
            return ResponseEntity.noContent().build();
        }

        final int limite = Math.max(
                MIN_ALERTA_ESTOQUE_LIMITE,
                appSettingService.getInt(
                        KEY_ALERTA_ESTOQUE_LIMITE,
                        DEFAULT_ALERTA_ESTOQUE_LIMITE
                )
        );
        final int qtdBaixo = produtoRepository
                .findComEstoqueBaixoScoped(resolveTenantId(), limite)
                .size();
        if (qtdBaixo <= 0) {
            return ResponseEntity.noContent().build();
        }

        final List<UsuarioEntity> usuarios = usuarioRepository.findAll()
                .stream()
                .filter(this::isAdminUser)
                .toList();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        final List<ClienteNotificacaoEntity> criadas = new ArrayList<>();
        final String titulo = "Estoque baixo";
        final String mensagem = qtdBaixo
                + " produtos com estoque acima de 0 e abaixo de "
                + limite
                + " unidades.";
        for (UsuarioEntity usuario : usuarios) {
            criadas.add(createNotification(
                    usuario,
                    TIPO_ESTOQUE,
                    titulo,
                    mensagem
            ));
        }
        saveAllIfAny(criadas);
        return ResponseEntity.ok().build();
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    /**
     * Creates one notification entity.
     *
     * @param usuario user entity
     * @param tipo notification type
     * @param titulo notification title
     * @param mensagem notification message
     * @return notification entity
     */
    private ClienteNotificacaoEntity createNotification(
            final UsuarioEntity usuario,
            final String tipo,
            final String titulo,
            final String mensagem
    ) {
        final ClienteNotificacaoEntity notificacao =
                new ClienteNotificacaoEntity();
        notificacao.setUsuario(usuario);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        return notificacao;
    }

    /**
     * Persists notification batch when list is not empty.
     *
     * @param criadas created notifications
     */
    private void saveAllIfAny(final List<ClienteNotificacaoEntity> criadas) {
        if (!criadas.isEmpty()) {
            notificacaoRepository.saveAll(criadas);
        }
    }

    /**
     * Checks if user has admin role.
     *
     * @param usuario user entity
     * @return true when user is admin
     */
    private boolean isAdminUser(final UsuarioEntity usuario) {
        if (usuario == null) {
            return false;
        }
        final Set<String> roles = usuario.getRoleNames();
        return roles.contains("ADMIN") || roles.contains("ROLE_ADMIN");
    }

    /**
     * Request payload for selected users notification.
     *
     * @param usuarioIds user ids
     * @param tipo notification type
     * @param titulo notification title
     * @param mensagem notification message
     */
    public record AdminNotificacaoRequest(
            @NotEmpty List<Long> usuarioIds,
            @NotBlank @Size(max = MAX_TIPO_LEN) String tipo,
            @NotBlank @Size(max = MAX_TITULO_LEN) String titulo,
            @NotBlank @Size(max = MAX_MENSAGEM_LEN) String mensagem
    ) {
    }

    /**
     * Request payload for broadcast notification.
     *
     * @param tipo notification type
     * @param titulo notification title
     * @param mensagem notification message
     */
    public record AdminNotificacaoBroadcastRequest(
            @NotBlank @Size(max = MAX_TIPO_LEN) String tipo,
            @NotBlank @Size(max = MAX_TITULO_LEN) String titulo,
            @NotBlank @Size(max = MAX_MENSAGEM_LEN) String mensagem
    ) {
    }

    /**
     * Request payload for single user notification.
     *
     * @param tipo notification type
     * @param titulo notification title
     * @param mensagem notification message
     */
    public record AdminNotificacaoSingleRequest(
            @NotBlank @Size(max = MAX_TIPO_LEN) String tipo,
            @NotBlank @Size(max = MAX_TITULO_LEN) String titulo,
            @NotBlank @Size(max = MAX_MENSAGEM_LEN) String mensagem
    ) {
    }
}
