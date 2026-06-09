package br.com.redemaisfarma.adapters.inbound.web.controller;

import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ItemPedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteFavoritoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ClienteNotificacaoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.ProdutoEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.entity.UsuarioEntity;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.PedidoJPARepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteFavoritoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteNotificacaoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.ProdutoRepository;
import br.com.redemaisfarma.adapters.outbound.persistence.repository.UsuarioRepository;
import br.com.redemaisfarma.adapters.outbound.auth.jwt.model.JwtPrincipal;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.service.CartService;
import br.com.redemaisfarma.application.service.validation.CartValidationService;
import br.com.redemaisfarma.application.service.PaymentMethodService;
import br.com.redemaisfarma.application.service.fiscal.PedidoFiscalSnapshotService;
import br.com.redemaisfarma.application.support.DeliveryCodeGenerator;
import br.com.redemaisfarma.application.support.PedidoStatusSupport;
import br.com.redemaisfarma.application.view.CartItemVM;
import br.com.redemaisfarma.application.view.CartSummaryVM;
import br.com.redemaisfarma.application.view.PaymentMethodVM;
import br.com.redemaisfarma.domain.enums.ModoEntrega;
import br.com.redemaisfarma.domain.enums.TipoPagamento;
import br.com.redemaisfarma.domain.financeiro.mercadopago.MercadoPagoCheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "App - Cliente Self-service", description = "Endpoints de perfil, carrinho, checkout, pedidos, notificações e favoritos do cliente autenticado")
@RestController
@RequestMapping("/api/cliente/me")
@Validated
public class ClienteSelfApiController {
    private static final long AVATAR_MAX_BYTES = 2L * 1024L * 1024L;
    private static final String DESCONHECIDO = "DESCONHECIDO";

    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final PaymentMethodService paymentMethodService;
    private final ImageStorageService imageStorageService;
    private final CartService cartService;
    private final ClienteRepository clienteRepository;
    private final PedidoJPARepository pedidoJPARepository;
    private final ClienteFavoritoRepository favoritoRepository;
    private final ClienteNotificacaoRepository notificacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoFiscalSnapshotService pedidoFiscalSnapshotService;
    private final MercadoPagoCheckoutService mercadoPagoCheckoutService;

    public ClienteSelfApiController(UsuarioRepository usuarioRepository,
                                    PedidoRepository pedidoRepository,
                                    PaymentMethodService paymentMethodService,
                                    ImageStorageService imageStorageService,
                                    CartService cartService,
                                    ClienteRepository clienteRepository,
                                    PedidoJPARepository pedidoJPARepository,
                                    ClienteFavoritoRepository favoritoRepository,
                                    ClienteNotificacaoRepository notificacaoRepository,
                                    ProdutoRepository produtoRepository,
                                    PedidoFiscalSnapshotService pedidoFiscalSnapshotService,
                                    MercadoPagoCheckoutService mercadoPagoCheckoutService) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.paymentMethodService = paymentMethodService;
        this.imageStorageService = imageStorageService;
        this.cartService = cartService;
        this.clienteRepository = clienteRepository;
        this.pedidoJPARepository = pedidoJPARepository;
        this.favoritoRepository = favoritoRepository;
        this.notificacaoRepository = notificacaoRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoFiscalSnapshotService = pedidoFiscalSnapshotService;
        this.mercadoPagoCheckoutService = mercadoPagoCheckoutService;
    }

    @Operation(summary = "Retorna os dados do cliente autenticado")
    @GetMapping
    public ClienteMeResponse me(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return ClienteMeResponse.from(usuario);
    }

    @Operation(summary = "Atualiza os dados do cliente autenticado")
    @PutMapping
    public ClienteMeResponse atualizar(@Valid @RequestBody ClienteMeUpdateRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String emailNormalizado = normalizarEmail(req.email());
        if (emailNormalizado == null || emailNormalizado.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um e-mail valido.");
        }
        usuarioRepository.findByEmailIgnoreCase(emailNormalizado).ifPresent(existente -> {
            if (!existente.getId().equals(usuario.getId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail ja cadastrado.");
            }
        });
        usuario.setNome(req.nome());
        usuario.setEmail(emailNormalizado);
        usuario.setCpf(req.cpf());
        usuario.setTelefone(normalizarTelefone(req.telefone()));
        usuario.setEndereco(normalizarEndereco(req.endereco()));
        usuarioRepository.save(usuario);
        return ClienteMeResponse.from(usuario);
    }

    @Operation(summary = "Atualiza o avatar do cliente autenticado")
    @PostMapping("/avatar")
    public ResponseEntity<?> atualizarAvatar(@RequestParam("file") MultipartFile file, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem para enviar.");
        }
        if (file.getSize() > AVATAR_MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem acima de 2MB.");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato invalido. Use PNG, JPG ou WEBP.");
        }
        try {
            String previousAvatarUrl = usuario.getAvatarUrl();
            String url = imageStorageService.saveUserAvatar(usuario.getId(), file);
            try {
                usuario.setAvatarUrl(url);
                usuarioRepository.save(usuario);
            } catch (RuntimeException ex) {
                restoreAvatar(usuario, previousAvatarUrl);
                imageStorageService.deleteUserAvatarByUrl(url);
                throw ex;
            }
            return ResponseEntity.ok(new AvatarResponse(url));
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Falha ao salvar avatar.", ex);
        }
    }

    @Operation(summary = "Lista os pedidos do cliente autenticado")
    @GetMapping("/pedidos")
    public List<PedidoResumoResponse> listarPedidos(Authentication auth) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return pedidoRepository.listarPorCliente(identidade.email(), identidade.cpf())
                .stream()
                .map(p -> PedidoResumoResponse.from(p, paymentMethodService))
                .toList();
    }

    @Operation(summary = "Detalha um pedido do cliente autenticado")
    @GetMapping("/pedidos/{id}")
    public PedidoDetalheResponse detalhe(@PathVariable("id") Long id, Authentication auth) {
        ClienteIdentidade identidade = resolveIdentidade(auth);
        if (identidade == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        PedidoEntity pedido = pedidoRepository.buscarDetalhePorCliente(id, identidade.email(), identidade.cpf())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        try {
            mercadoPagoCheckoutService.ensureCheckoutForPedido(pedido);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
            // Keep returning the order even if checkout regeneration fails.
        }
        return PedidoDetalheResponse.from(pedido, paymentMethodService);
    }

    @Operation(summary = "Retorna o resumo atual do carrinho do cliente")
    @GetMapping("/carrinho")
    public CartSummaryResponse carrinho(HttpSession session) {
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @Operation(summary = "Adiciona um item ao carrinho")
    @PostMapping("/carrinho")
    public CartSummaryResponse adicionar(@Valid @RequestBody CartItemRequest req, HttpSession session) {
        CartValidationService.CartValidationResult validation = cartService.validateAdd(session, req.produtoId(), req.quantidade());
        if (!validation.valid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, validation.message());
        }
        cartService.addItem(session, req.produtoId(), req.quantidade());
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @Operation(summary = "Atualiza a quantidade de um item do carrinho")
    @PutMapping("/carrinho/{produtoId}")
    public CartSummaryResponse atualizar(@PathVariable("produtoId") Long produtoId,
                                         @Valid @RequestBody CartUpdateRequest req,
                                         HttpSession session) {
        cartService.updateItem(session, produtoId, req.quantidade());
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @Operation(summary = "Remove um item do carrinho")
    @DeleteMapping("/carrinho/{produtoId}")
    public CartSummaryResponse remover(@PathVariable("produtoId") Long produtoId, HttpSession session) {
        cartService.removeItem(session, produtoId);
        return CartSummaryResponse.from(cartService.buildSummary(session));
    }

    @Operation(summary = "Retorna o resumo do checkout")
    @GetMapping("/checkout/resumo")
    public CheckoutResumoResponse resumo(HttpSession session) {
        CartSummaryVM summary = cartService.buildSummary(session);
        List<PaymentMethodResponse> methods = paymentMethodService.listActiveMethods()
                .stream()
                .map(PaymentMethodResponse::from)
                .toList();
        return new CheckoutResumoResponse(CartSummaryResponse.from(summary), methods);
    }

    @Operation(summary = "Finaliza o pedido do carrinho")
    @PostMapping("/checkout/finalizar")
    public CheckoutFinalizarResponse finalizar(@Valid @RequestBody CheckoutFinalizarRequest req,
                                               Authentication auth,
                                               HttpServletRequest request,
                                               HttpSession session) {
        if (!paymentMethodService.isActiveValue(req.pagamento())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo de pagamento invalido.");
        }
        TipoPagamento tipoPagamento = resolveTipoPagamento(req.pagamento());
        if (tipoPagamento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Metodo de pagamento invalido.");
        }

        Optional<ClienteEntity> clienteOpt = resolveClienteLogado(auth, normalize(req.email()), normalize(req.cpf()));
        if (clienteOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente nao encontrado.");
        }

        CartService.CartOrderData orderData = cartService.buildOrderData(session);
        if (orderData.getInvalidItems() != null && !orderData.getInvalidItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existem itens indisponiveis no carrinho.");
        }
        if (orderData.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrinho vazio.");
        }

        if (!paymentMethodService.isOfflineValue(req.pagamento())) {
            String tenantId = resolveTenantId(request, auth);
            try {
                mercadoPagoCheckoutService.assertReadyForOnlineCheckout(
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                req.nome(),
                                req.email(),
                                req.cpf(),
                                tenantId,
                                ""
                        )
                );
            } catch (IllegalStateException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage()
                );
            }
        }

        Optional<UsuarioEntity> usuarioLogado = localizarUsuario(auth);
        ClienteEntity cliente = clienteOpt.get();
        PedidoEntity pedido = new PedidoEntity();
        pedido.setCliente(cliente);
        pedido.setStatus(PedidoStatusSupport.initialStatus(
                paymentMethodService.isOfflineValue(req.pagamento())
        ));
        pedido.setTotal(orderData.getTotal());
        pedido.setTipoPagamento(tipoPagamento);
        pedido.setMetodoPagamento(req.pagamento());
        String enderecoEntrega = resolveEnderecoEntregaCheckout(req.enderecoEntrega(), usuarioLogado.orElse(null));
        pedido.setModoEntrega(enderecoEntrega != null ? ModoEntrega.ENTREGA : ModoEntrega.RETIRADA);
        pedido.setEnderecoEntrega(enderecoEntrega);
        pedido.setCodigoEntrega(DeliveryCodeGenerator.nextCode());
        pedido.setCodigoEntregaGeradoEm(LocalDateTime.now());
        pedido.setCodigoEntregaConfirmadoEm(null);
        orderData.getItems().forEach(pedido::addItem);
        PedidoEntity saved = pedidoJPARepository.save(pedido);
        pedidoFiscalSnapshotService.capture(
                saved,
                new PedidoFiscalSnapshotService.SnapshotRequest(
                        "CLIENTE_API",
                        req.nome(),
                        req.cpf(),
                        req.email(),
                        cliente.getTelefone(),
                        saved.getEnderecoEntrega(),
                        BigDecimal.ZERO
                )
        );
        if (!paymentMethodService.isOfflineValue(req.pagamento())) {
            try {
                mercadoPagoCheckoutService.createCheckoutForPedido(
                        saved,
                        new MercadoPagoCheckoutService.CheckoutRequest(
                                req.nome(),
                                req.email(),
                                req.cpf(),
                                resolveTenantId(request, auth),
                                ""
                        )
                );
            } catch (IllegalStateException ignored) {
                // The order is persisted and the checkout link can be retried later.
            }
        }
        cartService.clear(session);

        String label = paymentMethodService.resolveLabel(req.pagamento());
        usuarioLogado.ifPresent(usuario -> {
            String mensagem = "Pedido #" + saved.getId() + " registrado. Pagamento: " + label + ".";
            salvarNotificacao(usuario, "PEDIDO", "Pedido registrado", mensagem);
        });
        return new CheckoutFinalizarResponse(
                saved.getId(),
                label,
                saved.getCodigoEntrega(),
                saved.getEnderecoEntrega(),
                saved.getGatewayCheckoutUrl(),
                mercadoPagoCheckoutService.resolvePaymentStatusLabel(saved),
                saved.getGatewayPaymentTicketUrl(),
                saved.getGatewayPixQrCode(),
                saved.getGatewayPixQrCodeBase64()
        );
    }

    @Operation(summary = "Lista os produtos favoritos do cliente")
    @GetMapping("/favoritos")
    public List<FavoritoResponse> favoritos(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return favoritoRepository.findByUsuarioId(usuario.getId())
                .stream()
                .map(FavoritoResponse::from)
                .toList();
    }

    @Operation(summary = "Marca um produto como favorito")
    @PostMapping("/favoritos")
    public FavoritoResponse adicionarFavorito(@Valid @RequestBody FavoritoRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (favoritoRepository.existsByUsuarioIdAndProdutoId(usuario.getId(), req.produtoId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Produto ja favoritado.");
        }
        ProdutoEntity produto = produtoRepository.findPublicById(req.produtoId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto nao encontrado ou indisponivel."
                ));
        ClienteFavoritoEntity favorito = new ClienteFavoritoEntity();
        favorito.setUsuario(usuario);
        favorito.setProduto(produto);
        ClienteFavoritoEntity saved = favoritoRepository.save(favorito);
        return FavoritoResponse.from(saved);
    }

    @Operation(summary = "Remove um favorito do cliente")
    @DeleteMapping("/favoritos/{produtoId}")
    public ResponseEntity<?> removerFavorito(@PathVariable("produtoId") Long produtoId, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        favoritoRepository.deleteByUsuarioIdAndProdutoId(usuario.getId(), produtoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Lista notificações do cliente")
    @GetMapping("/notificacoes")
    public NotificacoesResponse notificacoes(Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        List<ClienteNotificacaoEntity> notificacoes = notificacaoRepository.findByUsuarioId(usuario.getId());
        long naoLidas = notificacaoRepository.countByUsuarioIdAndLidaFalse(usuario.getId());
        return new NotificacoesResponse(
                notificacoes.stream().map(NotificacaoResponse::from).toList(),
                naoLidas
        );
    }

    @Operation(summary = "Marca notificações como lidas")
    @PostMapping("/notificacoes/lidas")
    public ResponseEntity<?> marcarLidas(@Valid @RequestBody NotificacaoLidasRequest req, Authentication auth) {
        UsuarioEntity usuario = localizarUsuario(auth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (req.ids() == null || req.ids().isEmpty()) {
            return ResponseEntity.ok().build();
        }
        notificacaoRepository.markAsRead(usuario.getId(), req.ids());
        return ResponseEntity.ok().build();
    }

    private Optional<UsuarioEntity> localizarUsuario(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailOrCpf(auth.getName());
    }

    private ClienteIdentidade resolveIdentidade(Authentication auth) {
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }
        Optional<UsuarioEntity> usuario = localizarUsuario(auth);
        if (usuario.isPresent()) {
            UsuarioEntity u = usuario.get();
            return new ClienteIdentidade(u.getEmail(), u.getCpf());
        }
        String principal = auth.getName().trim();
        return new ClienteIdentidade(principal, principal);
    }

    private String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private Optional<ClienteEntity> resolveClienteLogado(Authentication auth, String email, String cpf) {
        if (auth == null || auth.getName() == null) {
            return Optional.empty();
        }
        String ident = normalize(auth.getName());
        Optional<ClienteEntity> byEmail = clienteRepository.findByEmailIgnoreCase(ident);
        Optional<ClienteEntity> byCpf = clienteRepository.findByCpf(ident);
        Optional<ClienteEntity> cliente = byEmail.isPresent() ? byEmail : byCpf;
        if (cliente.isEmpty()) {
            return Optional.empty();
        }
        ClienteEntity found = cliente.get();
        if (!email.isBlank() && !email.equalsIgnoreCase(found.getEmail())) {
            return Optional.empty();
        }
        if (!cpf.isBlank() && !normalizeCpf(cpf).equals(normalizeCpf(found.getCpf()))) {
            return Optional.empty();
        }
        return Optional.of(found);
    }

    private TipoPagamento resolveTipoPagamento(String value) {
        if (value == null) return null;
        return switch (value) {
            case "pix" -> TipoPagamento.PIX;
            case "boleto" -> TipoPagamento.BOLETO;
            case "credito" -> TipoPagamento.CARTAO_CREDITO;
            case "debito" -> TipoPagamento.CARTAO_DEBITO;
            case "dinheiro" -> TipoPagamento.DINHEIRO;
            default -> value.startsWith("custom:") ? TipoPagamento.CUSTOM : null;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeCpf(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) return null;
        String trimmed = telefone.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private String normalizarEndereco(String endereco) {
        if (endereco == null) return null;
        String trimmed = endereco.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return trimmed.length() > 255 ? trimmed.substring(0, 255) : trimmed;
    }

    private String resolveEnderecoEntregaCheckout(String requestEndereco, UsuarioEntity usuario) {
        String enderecoDoRequest = normalizarEndereco(requestEndereco);
        if (enderecoDoRequest != null) {
            return enderecoDoRequest;
        }
        if (usuario == null) {
            return null;
        }
        return normalizarEndereco(usuario.getEndereco());
    }

    private String resolveTenantId(
            HttpServletRequest request,
            Authentication auth
    ) {
        if (auth != null && auth.getPrincipal() instanceof JwtPrincipal jwtPrincipal) {
            String tenant = jwtPrincipal.getTenant();
            if (tenant != null && !tenant.isBlank()) {
                return tenant.trim();
            }
        }
        if (request == null) {
            return "";
        }
        Object claimsAttr = request.getAttribute("jwt.claims");
        if (claimsAttr instanceof java.util.Map<?, ?> rawClaims) {
            Object tenant = rawClaims.get("tenant");
            if (tenant == null) {
                tenant = rawClaims.get("tenantId");
            }
            return tenant == null ? "" : tenant.toString().trim();
        }
        return "";
    }

    private record ClienteIdentidade(String email, String cpf) {}

    public record ClienteMeResponse(
            Long id,
            String nome,
            String email,
            String cpf,
            String telefone,
            String endereco,
            String avatarUrl
    ) {
        static ClienteMeResponse from(UsuarioEntity usuario) {
            return new ClienteMeResponse(
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getEmail(),
                    usuario.getCpf(),
                    usuario.getTelefone(),
                    usuario.getEndereco(),
                    usuario.getAvatarUrl()
            );
        }
    }

    public record ClienteMeUpdateRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 150) String email,
            String cpf,
            @Size(max = 25) String telefone,
            @Size(max = 200) String endereco
    ) {}

    private void restoreAvatar(final UsuarioEntity usuario, final String previousAvatarUrl) {
        try {
            usuario.setAvatarUrl(previousAvatarUrl);
            usuarioRepository.save(usuario);
        } catch (RuntimeException ignored) {
            // Melhor esforço para evitar avatar apontando para arquivo removido.
        }
    }

    public record AvatarResponse(String avatarUrl) {}

    public record CartItemRequest(@NotNull Long produtoId, @Positive int quantidade) {}

    public record CartUpdateRequest(@Positive int quantidade) {}

    public record CartItemResponse(
            Long produtoId,
            String nome,
            String imagem,
            BigDecimal preco,
            Integer quantidade,
            BigDecimal subtotal,
            boolean invalido,
            String motivo,
            Integer estoque
    ) {
        static CartItemResponse from(CartItemVM item) {
            return new CartItemResponse(
                    item.produtoId(),
                    item.nome(),
                    item.imagem(),
                    item.precoUnitario(),
                    item.quantidade(),
                    item.subtotal(),
                    item.invalid(),
                    item.issue(),
                    item.estoque()
            );
        }
    }

    public record CartSummaryResponse(
            List<CartItemResponse> items,
            BigDecimal subtotal,
            BigDecimal total,
            boolean hasInvalidItems
    ) {
        static CartSummaryResponse from(CartSummaryVM summary) {
            List<CartItemResponse> items = summary.items().stream().map(CartItemResponse::from).toList();
            return new CartSummaryResponse(items, summary.subtotal(), summary.total(), summary.hasInvalidItems());
        }
    }

    public record PaymentMethodResponse(String value, String label, String type) {
        static PaymentMethodResponse from(PaymentMethodVM vm) {
            return new PaymentMethodResponse(vm.value(), vm.label(), vm.tipo());
        }
    }

    public record CheckoutResumoResponse(
            CartSummaryResponse carrinho,
            List<PaymentMethodResponse> metodosPagamento
    ) {}

    public record CheckoutFinalizarRequest(
            @NotBlank String nome,
            @NotBlank String cpf,
            @NotBlank @Email String email,
            @NotBlank String pagamento,
            @Size(max = 255) String enderecoEntrega
    ) {}

    public record CheckoutFinalizarResponse(
            Long pedidoId,
            String paymentMethodLabel,
            String codigoEntrega,
            String enderecoEntrega,
            String gatewayCheckoutUrl,
            String gatewayPaymentStatus,
            String gatewayPaymentTicketUrl,
            String gatewayPixQrCode,
            String gatewayPixQrCodeBase64
    ) {}

    public record FavoritoRequest(@NotNull Long produtoId) {}

    public record FavoritoResponse(
            Long produtoId,
            String nome,
            String imagem,
            BigDecimal preco,
            LocalDateTime createdAt
    ) {
        static FavoritoResponse from(ClienteFavoritoEntity favorito) {
            ProdutoEntity produto = favorito.getProduto();
            return new FavoritoResponse(
                    produto != null ? produto.getId() : null,
                    produto != null ? produto.getNome() : null,
                    produto != null ? produto.getImagem() : null,
                    produto != null ? produto.getPrecoVenda() : null,
                    favorito.getCreatedAt()
            );
        }
    }

    public record NotificacaoResponse(
            Long id,
            String tipo,
            String titulo,
            String mensagem,
            Boolean lida,
            LocalDateTime createdAt
    ) {
        static NotificacaoResponse from(ClienteNotificacaoEntity notificacao) {
            return new NotificacaoResponse(
                    notificacao.getId(),
                    notificacao.getTipo(),
                    notificacao.getTitulo(),
                    notificacao.getMensagem(),
                    notificacao.getLida(),
                    notificacao.getCreatedAt()
            );
        }
    }

    public record NotificacoesResponse(
            List<NotificacaoResponse> items,
            long unreadCount
    ) {}

    public record NotificacaoLidasRequest(@NotNull Collection<Long> ids) {}

    private void salvarNotificacao(UsuarioEntity usuario, String tipo, String titulo, String mensagem) {
        ClienteNotificacaoEntity notificacao = new ClienteNotificacaoEntity();
        notificacao.setUsuario(usuario);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacaoRepository.save(notificacao);
    }

    public record PedidoResumoResponse(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento
    ) {
        static PedidoResumoResponse from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            return new PedidoResumoResponse(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : DESCONHECIDO,
                    resolveMetodoLabel(p, paymentMethodService)
            );
        }
    }

    public record PedidoItemResponse(
            Long produtoId,
            String produto,
            Integer quantidade,
            BigDecimal subtotal
    ) {
        static PedidoItemResponse from(ItemPedidoEntity i) {
            Long produtoId = i.getProduto() != null ? i.getProduto().getId() : null;
            String nome = i.getProduto() != null ? i.getProduto().getNome() : "Produto";
            return new PedidoItemResponse(produtoId, nome, i.getQuantidade(), i.getSubtotal());
        }
    }

    public record PedidoDetalheResponse(
            Long id,
            LocalDateTime data,
            BigDecimal total,
            String status,
            String metodoPagamento,
            String enderecoEntrega,
            String codigoEntrega,
            LocalDateTime codigoEntregaConfirmadoEm,
            String gatewayCheckoutUrl,
            String gatewayPaymentStatus,
            String gatewayPaymentTicketUrl,
            String gatewayPixQrCode,
            String gatewayPixQrCodeBase64,
            List<PedidoItemResponse> itens
    ) {
        static PedidoDetalheResponse from(PedidoEntity p, PaymentMethodService paymentMethodService) {
            List<PedidoItemResponse> itens = p.getItens() == null
                    ? List.of()
                    : p.getItens().stream().map(PedidoItemResponse::from).toList();
            return new PedidoDetalheResponse(
                    p.getId(),
                    p.getData(),
                    p.getTotal(),
                    p.getStatus() != null ? p.getStatus().name() : DESCONHECIDO,
                    resolveMetodoLabel(p, paymentMethodService),
                    p.getEnderecoEntrega(),
                    p.getCodigoEntrega(),
                    p.getCodigoEntregaConfirmadoEm(),
                    p.getGatewayCheckoutUrl(),
                    p.getGatewayPaymentStatus(),
                    p.getGatewayPaymentTicketUrl(),
                    p.getGatewayPixQrCode(),
                    p.getGatewayPixQrCodeBase64(),
                    itens
            );
        }
    }

    private static String resolveMetodoLabel(PedidoEntity p, PaymentMethodService paymentMethodService) {
        if (p.getMetodoPagamento() != null && !p.getMetodoPagamento().isBlank()) {
            return paymentMethodService.resolveLabel(p.getMetodoPagamento());
        }
        return p.getTipoPagamento() != null ? p.getTipoPagamento().name() : DESCONHECIDO;
    }
}
