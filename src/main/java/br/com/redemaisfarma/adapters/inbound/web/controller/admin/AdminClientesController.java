package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/clientes")
public class AdminClientesController {

    /**
     * First page index.
     */
    private static final int PAGE_MIN = 0;

    /**
     * Minimum allowed page size.
     */
    private static final int SIZE_MIN = 10;

    /**
     * Maximum allowed page size.
     */
    private static final int SIZE_MAX = 200;

    /**
     * Default page size request parameter.
     */
    private static final String SIZE_DEFAULT_PARAM = "50";
    private static final String PAPEL_CLIENTE = "CLIENTE";
    private static final String PAPEL_CLIENTE_ROLE = "ROLE_CLIENTE";
    private static final String PAPEL_USER = "USER";
    private static final String PAPEL_USER_ROLE = "ROLE_USER";

    /**
     * Repository for customer data.
     */
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SessionRegistry sessionRegistry;

    /**
     * Creates controller with repository dependency.
     *
     * @param repository customer repository
     */
    public AdminClientesController(
            final ClienteRepository repository,
            final UsuarioRepository usuarioRepository,
            final SessionRegistry sessionRegistry
    ) {
        this.clienteRepository = repository;
        this.usuarioRepository = usuarioRepository;
        this.sessionRegistry = sessionRegistry;
    }

    /**
     * Lists customers for admin screen with filters and pagination.
     *
     * @param q optional search term
     * @param status optional status filter
     * @param page page index
     * @param size page size
     * @param model view model
     * @return customers list page
     */
    @GetMapping({"", "/", "/lista"})
    public String lista(
            @RequestParam(value = "q", required = false) final String q,
            @RequestParam(value = "status", required = false)
            final String status,
            @RequestParam(value = "page", defaultValue = "0") final int page,
            @RequestParam(value = "size", defaultValue = SIZE_DEFAULT_PARAM)
            final int size,
            final Model model
    ) {
        final String termo = normalize(q);
        final String statusSelecionado = normalizeStatus(status);
        final Boolean ativo = toAtivo(statusSelecionado).orElse(null);
        final int safePage = Math.max(PAGE_MIN, page);
        final int safeSize = Math.clamp(size, SIZE_MIN, SIZE_MAX);

        final Page<ClienteEntity> pageData = clienteRepository.searchAdmin(
                termo,
                ativo,
                PageRequest.of(
                        safePage,
                        safeSize,
                        Sort.by(Sort.Direction.DESC, "id")
                )
        );

        List<ClienteEntity> clientesPagina = pageData.getContent();
        long totalFiltrados = pageData.getTotalElements();
        long total = clienteRepository.count();
        long ativos = clienteRepository.countByAtivoTrue();

        if (total == 0L && (clientesPagina == null || clientesPagina.isEmpty())) {
            final List<ClienteEntity> fallback = buildFallbackFromUsuarios(
                    termo,
                    ativo,
                    safePage,
                    safeSize
            );
            clientesPagina = fallback;
            totalFiltrados = fallback.size();
            total = totalFiltrados;
            ativos = fallback.stream()
                    .filter(ClienteEntity::isAtivo)
                    .count();
        }
        final long inativos = Math.max(PAGE_MIN, total - ativos);
        final Map<Long, Boolean> onlineClienteMap = resolveOnlineMap(clientesPagina);
        final long totalOnline = onlineClienteMap.values().stream()
                .filter(Boolean.TRUE::equals)
                .count();

        model.addAttribute("clientes", clientesPagina);
        model.addAttribute("totalClientes", total);
        model.addAttribute("totalClientesAtivos", ativos);
        model.addAttribute("totalClientesInativos", inativos);
        model.addAttribute("totalClientesOnline", totalOnline);
        model.addAttribute("onlineClienteMap", onlineClienteMap);
        model.addAttribute("statusSelecionado", statusSelecionado);
        model.addAttribute("totalFiltrados", totalFiltrados);

        return "pages/admin/clientes/lista";
    }

    private List<ClienteEntity> buildFallbackFromUsuarios(
            final String termo,
            final Boolean ativo,
            final int safePage,
            final int safeSize
    ) {
        final Map<Long, UsuarioEntity> merged = new LinkedHashMap<>();
        mergeById(merged, resolveUsuariosCliente(termo));
        mergeById(merged, resolveUsuariosOnline(termo));
        final List<UsuarioEntity> usuarios = merged.values().stream().toList();
        final List<ClienteEntity> base = usuarios.stream()
                .map(this::toClienteView)
                .filter(cliente -> ativo == null || cliente.isAtivo() == ativo)
                .toList();
        if (base.isEmpty()) {
            return List.of();
        }
        final int from = safePage * safeSize;
        if (from >= base.size()) {
            return List.of();
        }
        final int to = Math.min(from + safeSize, base.size());
        return base.subList(from, to);
    }

    private List<UsuarioEntity> resolveUsuariosCliente(final String termo) {
        final Map<Long, UsuarioEntity> merged = new LinkedHashMap<>();
        mergeById(merged, this.usuarioRepository.search(termo, PAPEL_CLIENTE));
        mergeById(merged, this.usuarioRepository.search(termo, PAPEL_CLIENTE_ROLE));
        mergeById(merged, this.usuarioRepository.search(termo, PAPEL_USER));
        mergeById(merged, this.usuarioRepository.search(termo, PAPEL_USER_ROLE));
        return merged.values().stream().toList();
    }

    private List<UsuarioEntity> resolveUsuariosOnline(final String termo) {
        final Map<Long, UsuarioEntity> merged = new LinkedHashMap<>();
        for (String ident : resolveOnlineIdentifiers()) {
            this.usuarioRepository.findByEmailOrCpf(ident)
                    .filter(usuario -> matchesTerm(usuario, termo))
                    .ifPresent(usuario -> merged.putIfAbsent(usuario.getId(), usuario));
        }
        return merged.values().stream().toList();
    }

    private static void mergeById(
            final Map<Long, UsuarioEntity> merged,
            final List<UsuarioEntity> users
    ) {
        if (users == null) {
            return;
        }
        for (UsuarioEntity user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            merged.putIfAbsent(user.getId(), user);
        }
    }

    private static boolean matchesTerm(final UsuarioEntity usuario, final String termo) {
        final String normalizedTerm = normalize(termo);
        if (usuario == null || normalizedTerm == null) {
            return true;
        }

        final String lowerTerm = normalizedTerm.toLowerCase(Locale.ROOT);
        final String nome = normalize(usuario.getNome());
        if (nome != null && nome.toLowerCase(Locale.ROOT).contains(lowerTerm)) {
            return true;
        }

        final String email = normalize(usuario.getEmail());
        if (email != null && email.toLowerCase(Locale.ROOT).contains(lowerTerm)) {
            return true;
        }

        final String cpf = normalizeIdentifier(usuario.getCpf());
        if (cpf == null) {
            return false;
        }
        final String termDigits = normalizedTerm.replaceAll("\\D", "");
        return !termDigits.isEmpty() && cpf.contains(termDigits);
    }

    private ClienteEntity toClienteView(final UsuarioEntity usuario) {
        final ClienteEntity cliente = new ClienteEntity();
        cliente.setId(usuario.getId());
        cliente.setNome(usuario.getNome());
        cliente.setEmail(usuario.getEmail());
        cliente.setTelefone(usuario.getTelefone());
        cliente.setCpf(usuario.getCpf());
        cliente.setAtivo(resolveUsuarioAtivo(usuario));
        cliente.setCreatedAt(resolveCreatedAt(usuario));
        return cliente;
    }

    private static boolean resolveUsuarioAtivo(final UsuarioEntity usuario) {
        final Integer tentativas = usuario.getTentativasFalhas();
        return tentativas == null || tentativas < 5;
    }

    private static LocalDateTime resolveCreatedAt(final UsuarioEntity usuario) {
        if (usuario.getCreatedAt() != null) {
            return usuario.getCreatedAt();
        }
        return usuario.getUpdatedAt();
    }

    private Map<Long, Boolean> resolveOnlineMap(final List<ClienteEntity> clientes) {
        final Map<Long, Boolean> map = new HashMap<>();
        if (clientes == null || clientes.isEmpty()) {
            return map;
        }
        final Set<String> onlineIdentifiers = resolveOnlineIdentifiers();
        for (ClienteEntity cliente : clientes) {
            if (cliente == null || cliente.getId() == null) {
                continue;
            }
            final String email = normalizeIdentifier(cliente.getEmail());
            final String cpf = normalizeIdentifier(cliente.getCpf());
            final boolean isOnline = (email != null && onlineIdentifiers.contains(email))
                    || (cpf != null && onlineIdentifiers.contains(cpf));
            map.put(cliente.getId(), isOnline);
        }
        return map;
    }

    private Set<String> resolveOnlineIdentifiers() {
        final Set<String> online = new HashSet<>();
        for (Object principal : this.sessionRegistry.getAllPrincipals()) {
            if (principal == null) {
                continue;
            }
            final List<SessionInformation> sessions = this.sessionRegistry.getAllSessions(principal, false);
            if (sessions == null || sessions.isEmpty()) {
                continue;
            }
            final String name = extractPrincipalName(principal);
            final String normalized = normalizeIdentifier(name);
            if (normalized != null) {
                online.add(normalized);
            }
        }
        return online;
    }

    private static String extractPrincipalName(final Object principal) {
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        return principal.toString();
    }

    private static String normalizeIdentifier(final String value) {
        final String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.contains("@")) {
            return normalized.toLowerCase(Locale.ROOT);
        }
        final String digits = normalized.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    /**
     * Normalizes free-text values.
     *
     * @param value source value
     * @return normalized value or null
     */
    private static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalizes status to upper-case enum-like value.
     *
     * @param value raw status
     * @return normalized status
     */
    private static String normalizeStatus(final String value) {
        final String normalized = normalize(value);
        if (normalized == null) {
            return "";
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    /**
     * Maps status filter to active flag.
     *
     * @param status normalized status
     * @return active flag when status is known
     */
    private static Optional<Boolean> toAtivo(final String status) {
        if ("ATIVO".equals(status)) {
            return Optional.of(Boolean.TRUE);
        }
        if ("INATIVO".equals(status)) {
            return Optional.of(Boolean.FALSE);
        }
        return Optional.empty();
    }
}
