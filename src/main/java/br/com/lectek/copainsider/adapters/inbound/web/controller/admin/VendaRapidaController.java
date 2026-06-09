package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.inbound.web.dto.ClienteLookupResponseDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.ProdutoBuscaDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarRequestDTO;
import br.com.lectek.copainsider.adapters.inbound.web.dto.VendaRapidaFinalizarResponseDTO;
import br.com.lectek.copainsider.adapters.outbound.persistence.entity.PedidoEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.ClienteRepository;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.PedidoRepository;
import br.com.lectek.copainsider.application.service.CaixaResumoService;
import br.com.lectek.copainsider.application.service.CaixaVendaRapidaService;
import br.com.lectek.copainsider.application.service.VendaRapidaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Profile("!test")
@Controller
@RequestMapping("/admin/vendas")
public class VendaRapidaController {

    /**
     * Default product search limit.
     */
    private static final int DEFAULT_SEARCH_LIMIT = 10;

    /**
     * Service for legacy quick sale flow.
     */
    private final VendaRapidaService vendaRapida;

    /**
     * Service for modern quick sale flow.
     */
    private final CaixaVendaRapidaService caixaService;

    /**
     * Service for cash summary.
     */
    private final CaixaResumoService caixaResumoService;

    /**
     * Repository for order reads.
     */
    private final PedidoRepository pedidoRepo;

    /**
     * Repository for customer lookup.
     */
    private final ClienteRepository clienteRepo;

    /**
     * Creates controller with dependencies.
     *
     * @param vendaRapidaService quick sale service
     * @param caixaVendaRapidaService modern quick sale service
     * @param caixaResumoServiceValue cash summary service
     * @param pedidoRepository order repository
     * @param clienteRepository customer repository
     */
    public VendaRapidaController(
            final VendaRapidaService vendaRapidaService,
            final CaixaVendaRapidaService caixaVendaRapidaService,
            final CaixaResumoService caixaResumoServiceValue,
            final PedidoRepository pedidoRepository,
            final ClienteRepository clienteRepository
    ) {
        this.vendaRapida = vendaRapidaService;
        this.caixaService = caixaVendaRapidaService;
        this.caixaResumoService = caixaResumoServiceValue;
        this.pedidoRepo = pedidoRepository;
        this.clienteRepo = clienteRepository;
    }

    /**
     * Renders quick sale page.
     *
     * @return quick sale view
     */
    @GetMapping("/rapida")
    public String page() {
        return "pages/admin/vendas/rapida";
    }

    /**
     * Searches products for quick sale.
     *
     * @param termo search term
     * @param limit max items
     * @return products list
     */
    @GetMapping("/rapida/produtos")
    @ResponseBody
    public List<ProdutoBuscaDTO> buscarProdutos(
            @RequestParam("q") final String termo,
            @RequestParam(value = "limit", defaultValue = "10")
            final int limit
    ) {
        final int normalizedLimit = limit <= 0 ? DEFAULT_SEARCH_LIMIT : limit;
        return caixaService.buscarProdutos(termo, normalizedLimit);
    }

    /**
     * Searches customer by CPF.
     *
     * @param cpf raw cpf
     * @return customer lookup payload
     */
    @GetMapping("/rapida/cliente")
    @ResponseBody
    public ClienteLookupResponseDTO buscarCliente(
            @RequestParam("cpf") final String cpf
    ) {
        final String clean = cpf == null ? "" : cpf.replaceAll("\\D", "");
        if (clean.isBlank()) {
            return new ClienteLookupResponseDTO(false, null, null);
        }
        return clienteRepo.findByCpf(clean)
                .map(cliente -> new ClienteLookupResponseDTO(
                        true,
                        cliente.getNome(),
                        cliente.getEmail()
                ))
                .orElseGet(
                        () -> new ClienteLookupResponseDTO(false, null, null)
                );
    }

    /**
     * Finalizes quick sale via JSON payload.
     *
     * @param request finalize request
     * @return finalize response
     */
    @PostMapping("/rapida/finalizar")
    @ResponseBody
    public VendaRapidaFinalizarResponseDTO finalizar(
            @Valid @RequestBody final VendaRapidaFinalizarRequestDTO request
    ) {
        return caixaService.finalizar(request);
    }

    /**
     * Returns cash summary for one day.
     *
     * @param dia day filter
     * @return cash summary payload
     */
    @GetMapping("/caixa/resumo")
    @ResponseBody
    public CaixaResumoService.CaixaResumo resumoCaixa(
            @RequestParam(value = "dia", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            final LocalDate dia
    ) {
        return caixaResumoService.resumoDia(dia);
    }

    /**
     * Renders receipt page for one order.
     *
     * @param id order id
     * @param model view model
     * @return receipt view
     */
    @GetMapping("/rapida/recibo/{id}")
    public String recibo(
            @PathVariable("id") final Long id,
            final Model model
    ) {
        final PedidoEntity pedido = pedidoRepo.buscarDetalheAdmin(id)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Pedido nao encontrado."
                        )
                );
        model.addAttribute("pedido", pedido);
        return "pages/admin/vendas/recibo";
    }

    /**
     * Registers sale using legacy redirect flow.
     *
     * @param refCliente customer reference
     * @param refProduto product reference
     * @param quantidade quantity
     * @param ra redirect attributes
     * @return redirect URL
     */
    @PostMapping("/rapida")
    public String vender(
            @RequestParam("cliente") final String refCliente,
            @RequestParam("produto") final String refProduto,
            @RequestParam("qtd") final int quantidade,
            final RedirectAttributes ra
    ) {
        try {
            final Long pedidoId = vendaRapida.criar(
                    refCliente,
                    refProduto,
                    quantidade
            );
            ra.addFlashAttribute("ok", "Venda registrada com sucesso.");
            return "redirect:/admin/pedidos/" + pedidoId;
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/admin/dashboard";
        }
    }
}
