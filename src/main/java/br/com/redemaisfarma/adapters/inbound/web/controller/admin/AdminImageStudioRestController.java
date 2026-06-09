package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import br.com.redemaisfarma.adapters.inbound.web.dto.ImageGenRequestDTO;
import br.com.redemaisfarma.adapters.outbound.persistence.jpa.ProdutoJpaRepository;
import br.com.redemaisfarma.application.core.tenant.TenantResolverService;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import br.com.redemaisfarma.application.service.AiGeneratedImageStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/imagens")
public class AdminImageStudioRestController {

    private final ImageStudioUseCase imageStudioUseCase;
    private final AiGeneratedImageStorageService generatedImageStorageService;
    private final ProdutoJpaRepository produtoJpaRepository;
    private final ObjectMapper objectMapper;
    @Autowired(required = false)
    private TenantResolverService tenantResolverService;

    public AdminImageStudioRestController(
            final ImageStudioUseCase imageStudioUseCase,
            final AiGeneratedImageStorageService generatedImageStorageService,
            final ProdutoJpaRepository produtoJpaRepository,
            final ObjectMapper objectMapper
    ) {
        this.imageStudioUseCase = imageStudioUseCase;
        this.generatedImageStorageService = generatedImageStorageService;
        this.produtoJpaRepository = produtoJpaRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping(
            value = "/generate",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<Object> generate(
            @RequestBody final ImageGenRequestDTO request,
            @RequestParam(name = "produtoId", required = false) final Long produtoId
    ) {
        return generateInternal(request, produtoId);
    }

    @PostMapping(
            value = "/generate",
            consumes = "multipart/form-data",
            produces = "application/json"
    )
    public ResponseEntity<Object> generateMultipart(
            @RequestParam(name = "preset") final String preset,
            @RequestParam(name = "prompt", required = false) final String prompt,
            @RequestParam(name = "vars", required = false) final String varsJson,
            @RequestParam(name = "inputImage", required = false) final MultipartFile inputImage,
            @RequestParam(name = "removeBackground", required = false) final Boolean removeBackground,
            @RequestParam(name = "upscale", required = false) final Boolean upscale,
            @RequestParam(name = "produtoId", required = false) final Long produtoId
    ) {
        try {
            final ImageGenRequestDTO request = new ImageGenRequestDTO(
                    preset,
                    prompt,
                    parseVars(varsJson),
                    toDataUri(inputImage),
                    Boolean.TRUE.equals(removeBackground),
                    Boolean.TRUE.equals(upscale)
            );
            return generateInternal(request, produtoId);
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body(new ApiError("Falha ao ler imagem enviada: " + ex.getMessage()));
        }
    }

    private ResponseEntity<Object> generateInternal(
            final ImageGenRequestDTO request,
            final Long produtoId
    ) {
        if (!ImageGenRequestDTO.Preset.isValid(request.preset())) {
            return ResponseEntity.badRequest().body(new ApiError("Preset de imagem invalido."));
        }
        final Long tenantId = resolveTenantId();
        if (produtoId != null
                && (tenantId == null
                    ? produtoJpaRepository.findById(produtoId)
                    : produtoJpaRepository.findByScopedId(tenantId, produtoId)).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError("Produto nao encontrado."));
        }

        try {
            final ImageStudioUseCase.GeneratedImageResult generated = imageStudioUseCase.generateSync(
                    request
            );
            final String storedUrl = generatedImageStorageService.persistGeneratedImage(
                    produtoId,
                    generated
            );
            try {
                if (produtoId != null) {
                    (tenantId == null
                            ? produtoJpaRepository.findById(produtoId)
                            : produtoJpaRepository.findByScopedId(tenantId, produtoId)).ifPresent(produto -> {
                        produto.addImagemProduto(storedUrl);
                        produto.definirImagemPrincipal(storedUrl);
                        produto.setUpdatedAt(LocalDateTime.now());
                        produtoJpaRepository.save(produto);
                    });
                }
            } catch (RuntimeException ex) {
                generatedImageStorageService.deletePersistedProductImage(storedUrl);
                throw ex;
            }

            return ResponseEntity.ok(new GenerateImageResponse(
                    produtoId,
                    storedUrl,
                    generated.provider(),
                    generated.sourcePrompt(),
                    generated.revisedPrompt(),
                    generated.mimeType()
            ));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiError("Falha ao persistir imagem gerada: " + ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiError(ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ApiError("Falha ao gerar imagem via IA: " + ex.getMessage()));
        }
    }

    private Long resolveTenantId() {
        if (this.tenantResolverService == null) {
            return null;
        }
        return this.tenantResolverService.resolveDefaultTenantId();
    }

    private Map<String, Object> parseVars(final String varsJson) throws IOException {
        if (varsJson == null || varsJson.isBlank()) {
            return Map.of();
        }
        final Map<String, Object> raw = objectMapper.readValue(
                varsJson,
                new TypeReference<LinkedHashMap<String, Object>>() { }
        );
        final Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static String toDataUri(final MultipartFile inputImage) throws IOException {
        if (inputImage == null || inputImage.isEmpty()) {
            return null;
        }
        final String contentType = inputImage.getContentType() == null || inputImage.getContentType().isBlank()
                ? "image/png"
                : inputImage.getContentType().trim();
        final String base64 = Base64.getEncoder().encodeToString(inputImage.getBytes());
        return "data:" + contentType + ";base64," + base64;
    }

    public record GenerateImageResponse(
            Long produtoId,
            String imageUrl,
            String provider,
            String sourcePrompt,
            String revisedPrompt,
            String mimeType
    ) {
    }

    public record ApiError(String message) {
    }
}
