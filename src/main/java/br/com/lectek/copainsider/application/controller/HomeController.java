package br.com.lectek.copainsider.application.controller;

import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ProdutoCategoriaRepository;
import br.com.lectek.copainsider.application.config.AppProps;
import br.com.lectek.copainsider.application.core.settings.AppSettingService;
import br.com.lectek.copainsider.application.service.CartService;
import br.com.lectek.copainsider.application.service.HomeCarouselConfigService;
import br.com.lectek.copainsider.application.service.HomeLayoutConfigService;
import br.com.lectek.copainsider.application.service.PaymentMethodService;
import br.com.lectek.copainsider.application.service.ProductCategorySectionService;
import br.com.lectek.copainsider.application.service.delivery.DeliveryPricingService;
import br.com.lectek.copainsider.application.view.CartSummaryVM;
import br.com.lectek.copainsider.application.view.DeliveryQuoteVM;
import br.com.lectek.copainsider.application.view.HomePageVM;
import br.com.lectek.copainsider.application.view.HomeSectionBlockVM;
import br.com.lectek.copainsider.application.view.PaymentMethodVM;
import br.com.lectek.copainsider.application.view.ProductCardVM;
import br.com.lectek.copainsider.application.view.ProductCategorySectionVM;
import br.com.lectek.copainsider.domain.catalogo.HomepageCatalogFacade;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private static final int DEFAULT_HOME_CATEGORY_SECTION_LIMIT = 6;

    @Generated
    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final HomepageCatalogFacade facade;
    private final PaymentMethodService paymentMethodService;
    private final CartService cartService;
    private final ProdutoCategoriaRepository categoriaRepository;
    private final HomeCarouselConfigService homeCarouselConfigService;
    private final HomeLayoutConfigService homeLayoutConfigService;
    private final ProductCategorySectionService productCategorySectionService;
    private final DeliveryPricingService deliveryPricingService;
    private final AppSettingService appSettingService;
    private final AppProps appProps;

    @Generated
    public HomeController(
            final HomepageCatalogFacade facade,
            final PaymentMethodService paymentMethodService,
            final CartService cartService,
            final ProdutoCategoriaRepository categoriaRepository,
            final HomeCarouselConfigService carouselConfigService,
            final HomeLayoutConfigService homeLayoutConfigService,
            final ProductCategorySectionService productCategorySectionService,
            final DeliveryPricingService deliveryPricingService,
            final AppSettingService appSettingService,
            final AppProps appProps
    ) {
        this.facade = facade;
        this.paymentMethodService = paymentMethodService;
        this.cartService = cartService;
        this.categoriaRepository = categoriaRepository;
        this.homeCarouselConfigService = carouselConfigService;
        this.homeLayoutConfigService = homeLayoutConfigService;
        this.productCategorySectionService = productCategorySectionService;
        this.deliveryPricingService = deliveryPricingService;
        this.appSettingService = appSettingService;
        this.appProps = appProps;
    }

    @GetMapping({"/", "/cliente", "/cliente/index"})
    public String exibirPaginaInicial(
            @RequestParam(value = "onboarding", required = false) final String onboarding,
            final Model model,
            final HttpSession session
    ) {
        HomePageVM vm = null;
        try {
            vm = facade.buildHomepage();
            log.debug("HomePageVM carregada? {}", vm != null ? 1 : 0);
        } catch (Exception ex) {
            log.error("Falha ao montar HomePageVM", ex);
        }

        final HomeLayoutConfigService.HomeLayoutConfig homeLayoutConfig =
                homeLayoutConfigService.load();
        final Map<String, String> homeCarouselStyles =
                homeCarouselConfigService.resolveStyles();
        final List<ProductCategorySectionVM> categorySections =
                productCategorySectionService.loadPublicSections(
                        DEFAULT_HOME_CATEGORY_SECTION_LIMIT
                );
        final List<HomeSectionBlockVM> resolvedHomeSections =
                resolveHomeSections(
                        vm,
                        homeLayoutConfig,
                        homeCarouselStyles,
                        categorySections
                );

        model.addAttribute("vm", vm);
        model.addAttribute("homeLayoutConfig", homeLayoutConfig);
        model.addAttribute("homeSections", resolvedHomeSections);
        model.addAttribute("homeTrustItems", homeLayoutConfig.trustItems());
        model.addAttribute("showWelcome", isOnboarding(onboarding));
        model.addAttribute(
                "homeCategorias",
                resolveHomeCategorias(homeLayoutConfig.quickCategoriesLimit())
        );
        model.addAttribute("homeCarouselStyles", homeCarouselStyles);
        model.addAttribute("page", "home");
        model.addAttribute("cartSummary", cartService.buildSummary(session));
        return "pages/cliente/index";
    }

    @GetMapping("/checkout")
    public String exibirCheckout(
            @org.springframework.web.bind.annotation.RequestParam(
                    value = "tenantId", required = false) final String tenantIdParam,
            final Model model,
            final HttpSession session,
            final Authentication authentication
    ) {
        if (tenantIdParam != null && !tenantIdParam.isBlank()) {
            session.setAttribute("tenantContextId", tenantIdParam);
        }
        final Object sessionTenant = session.getAttribute("tenantContextId");
        if (sessionTenant != null) {
            model.addAttribute("tenantContextId", sessionTenant.toString());
        }
        model.addAttribute("page", "checkout");
        final List<PaymentMethodVM> methods = paymentMethodService.listActiveMethods();
        model.addAttribute("paymentMethods", methods);
        model.addAttribute("paymentMethodsEmpty", methods.isEmpty());
        model.addAttribute(
                "checkoutTenantId",
                resolveCheckoutTenantId(model)
        );

        final CartSummaryVM summary = cartService.buildSummary(session);
        final BigDecimal subtotalValue =
                summary.subtotal() != null ? summary.subtotal() : BigDecimal.ZERO;
        final BigDecimal cartTotalValue =
                summary.total() != null ? summary.total() : BigDecimal.ZERO;
        final DeliveryQuoteVM deliveryQuote = deliveryPricingService
                .quoteForCheckout(
                        normalizeText((String) model.asMap().get("checkoutInputEndereco")),
                        authentication
                );
        final BigDecimal standardShippingValue =
                deliveryQuote.available()
                        ? deliveryQuote.standardShippingAmount()
                        : BigDecimal.ZERO;
        final BigDecimal priorityShippingValue =
                deliveryQuote.available()
                        ? deliveryQuote.priorityShippingAmount()
                        : BigDecimal.ZERO;
        final BigDecimal checkoutTotalValue =
                cartTotalValue.add(standardShippingValue);
        final BigDecimal discountValue =
                subtotalValue.compareTo(cartTotalValue) > 0
                        ? subtotalValue.subtract(cartTotalValue)
                        : BigDecimal.ZERO;

        model.addAttribute("cartSummary", summary);
        model.addAttribute("cartItems", summary.items());
        model.addAttribute("cartEmpty", summary.items().isEmpty());
        model.addAttribute("cartHasInvalidItems", summary.hasInvalidItems());
        model.addAttribute("checkoutDeliveryQuote", deliveryQuote);
        model.addAttribute("checkoutSubtotalValue", subtotalValue);
        model.addAttribute("checkoutBaseTotalValue", cartTotalValue);
        model.addAttribute("checkoutTotalValue", checkoutTotalValue);
        model.addAttribute("checkoutDiscountValue", discountValue);
        model.addAttribute("checkoutShippingValue", standardShippingValue);
        model.addAttribute("checkoutStandardShippingValue", standardShippingValue);
        model.addAttribute("checkoutPriorityShippingValue", priorityShippingValue);
        model.addAttribute(
                "checkoutPriorityShippingSurchargeValue",
                deliveryQuote.prioritySurcharge()
        );
        model.addAttribute(
                "checkoutPickupEnabled",
                readBooleanSetting("retirada.ativa", true)
        );
        model.addAttribute(
                "checkoutPickupSchedule",
                normalizeText(appSettingService.getOrDefault("retirada.horario", ""))
        );
        model.addAttribute(
                "checkoutPickupInstructions",
                normalizeText(appSettingService.getOrDefault("retirada.instrucoes", ""))
        );
        model.addAttribute(
                "checkoutPickupStoreName",
                normalizeText(appProps.getStoreDisplayName())
        );
        model.addAttribute(
                "checkoutPickupAddress",
                normalizeText(appProps.getAddressDisplay())
        );
        return "pages/cliente/checkout/checkout";
    }

    @GetMapping("/carrinho")
    public String exibirCarrinho(final Model model, final HttpSession session) {
        final CartSummaryVM summary = cartService.buildSummary(session);
        model.addAttribute("cartSummary", summary);
        model.addAttribute("cartItems", summary.items());
        model.addAttribute("cartEmpty", summary.items().isEmpty());
        model.addAttribute("cartHasInvalidItems", summary.hasInvalidItems());
        model.addAttribute("page", "carrinho");
        return "pages/cliente/carrinho/carrinho";
    }

    @GetMapping("/sobre")
    public String exibirSobre(final Model model, final HttpSession session) {
        model.addAttribute("page", "sobre");
        model.addAttribute("active", "sobre");
        model.addAttribute("cartSummary", cartService.buildSummary(session));
        return "pages/cliente/sobre-page";
    }

    private List<HomeSectionBlockVM> resolveHomeSections(
            final HomePageVM vm,
            final HomeLayoutConfigService.HomeLayoutConfig layoutConfig,
            final Map<String, String> carouselStyles,
            final List<ProductCategorySectionVM> categorySections
    ) {
        if (layoutConfig == null) {
            return List.of();
        }

        final Map<String, List<ProductCardVM>> sourceMap = Map.of(
                "destaque",
                safeCards(vm == null ? null : vm.destaque()),
                "paraVoce",
                safeCards(vm == null ? null : vm.paraVoce()),
                "novidades",
                safeCards(vm == null ? null : vm.novidades()),
                "maisVendidos",
                safeCards(vm == null ? null : vm.maisVendidos())
        );

        return layoutConfig.sections().stream()
                .filter(HomeLayoutConfigService.HomeSectionConfig::enabled)
                .map(section -> buildHomeSection(
                        section,
                        sourceMap,
                        carouselStyles,
                        categorySections
                ))
                .filter(section -> hasRenderableContent(section))
                .sorted(Comparator.comparingInt(HomeSectionBlockVM::order))
                .toList();
    }

    private HomeSectionBlockVM buildHomeSection(
            final HomeLayoutConfigService.HomeSectionConfig section,
            final Map<String, List<ProductCardVM>> sourceMap,
            final Map<String, String> carouselStyles,
            final List<ProductCategorySectionVM> categorySections
    ) {
        if ("categorySections".equals(section.key())) {
            return new HomeSectionBlockVM(
                    section.key(),
                    "category-sections",
                    section.title(),
                    section.subtitle(),
                    section.layout(),
                    section.order() == null ? 999 : section.order(),
                    section.ctaLabel(),
                    section.ctaHref(),
                    carouselStyles.getOrDefault(section.key(), "classic"),
                    List.of(),
                    safeCategorySections(categorySections)
            );
        }

        return new HomeSectionBlockVM(
                section.key(),
                "products",
                section.title(),
                section.subtitle(),
                section.layout(),
                section.order() == null ? 999 : section.order(),
                section.ctaLabel(),
                section.ctaHref(),
                carouselStyles.getOrDefault(section.key(), "classic"),
                sourceMap.getOrDefault(section.key(), List.of()),
                List.of()
        );
    }

    private List<ProductCardVM> safeCards(final List<ProductCardVM> cards) {
        return cards == null ? List.of() : List.copyOf(cards);
    }

    private List<ProductCategorySectionVM> safeCategorySections(
            final List<ProductCategorySectionVM> sections
    ) {
        return sections == null ? List.of() : List.copyOf(sections);
    }

    private boolean hasRenderableContent(final HomeSectionBlockVM section) {
        if (section == null) {
            return false;
        }
        if ("category-sections".equals(section.type())) {
            return !section.categorySections().isEmpty();
        }
        return !section.items().isEmpty();
    }

    private boolean isOnboarding(final String value) {
        if (value == null) {
            return false;
        }
        return "1".equals(value)
                || "true".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value);
    }

    private List<String> resolveHomeCategorias(final int limit) {
        final List<String> categorias = categoriaRepository.findAllNomes();
        if (categorias == null || categorias.isEmpty()) {
            return List.of();
        }
        return categorias.stream()
                .map(this::normalizeCategoria)
                .filter(nome -> !nome.isBlank())
                .distinct()
                .limit(Math.max(1, limit))
                .toList();
    }

    private String normalizeCategoria(final String value) {
        return value == null ? "" : value.trim();
    }

    private boolean readBooleanSetting(final String key, final boolean defaultValue) {
        final String defaultRaw = defaultValue ? "1" : "0";
        final String value = normalizeText(appSettingService.getOrDefault(key, defaultRaw));
        if (value.isBlank()) {
            return defaultValue;
        }
        return "1".equals(value)
                || "true".equalsIgnoreCase(value)
                || "sim".equalsIgnoreCase(value)
                || "yes".equalsIgnoreCase(value);
    }

    private String resolveCheckoutTenantId(final Model model) {
        final String flashedTenantId = normalizeText(
                stringify(model.asMap().get("checkoutInputTenantId"))
        );
        if (!flashedTenantId.isBlank()) {
            return flashedTenantId;
        }
        return normalizeText(stringify(model.asMap().get("tenantContextId")));
    }

    private String normalizeText(final String value) {
        return value == null ? "" : value.trim();
    }

    private String stringify(final Object value) {
        return value == null ? "" : value.toString();
    }
}
