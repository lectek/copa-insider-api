package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.messaging.ProductImageRequestedEvent;
import br.com.lectek.copainsider.application.port.inbound.ImageStudioUseCase;
import br.com.lectek.copainsider.application.port.outbound.ProductImageJobRepository;
import br.com.lectek.copainsider.application.port.outbound.ProdutoRepositoryPort;
import br.com.lectek.copainsider.domain.ai.ProductPromptFactory;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductImageJobServiceTest {

    @TempDir
    Path tempDir;

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void processShouldPersistGeneratedImageAsPng() throws Exception {
        final ProdutoRepositoryPort produtos = mock(ProdutoRepositoryPort.class);
        final ProductImageJobRepository jobs = mock(ProductImageJobRepository.class);
        final ImageStudioUseCase imageStudio = mock(ImageStudioUseCase.class);
        final ProductPromptFactory promptFactory = new ProductPromptFactory();

        final br.com.lectek.copainsider.application.core.media.ImageStorageProperties props =
                new br.com.lectek.copainsider.application.core.media.ImageStorageProperties();
        final Path mediaProductsDir = tempDir.resolve("media").resolve("products");
        props.setDir(mediaProductsDir.toString());
        props.setPublicBase("/media/products");
        final br.com.lectek.copainsider.application.core.media.ImageStorageService imageStorageService =
                new br.com.lectek.copainsider.application.core.media.ImageStorageService(props);
        final AiGeneratedImageStorageService generatedImageStorageService =
                new AiGeneratedImageStorageService(imageStorageService);

        final ProductImageJobService service = new ProductImageJobService(
                produtos,
                jobs,
                imageStudio,
                promptFactory,
                generatedImageStorageService
        );

        final Long productId = 7L;
        when(produtos.findById(productId))
                .thenReturn(Optional.of(new ProdutoRepositoryPort.ProdutoDTO(
                        productId,
                        "Dipirona 500mg",
                        "Dipirona 500mg",
                        "Medicacoes",
                        "7890000000000",
                        "Generico",
                        null
                )));
        when(jobs.createQueued(eq(productId), anyString()))
                .thenReturn(new ProductImageJobRepository.Job(
                        1L,
                        productId,
                        ProductImageJobRepository.Status.QUEUED,
                        null,
                        null,
                        "fingerprint",
                        Instant.now(),
                        Instant.now()
                ));

        final byte[] jpegBytes = createSampleJpeg();
        this.server = HttpServer.create(new InetSocketAddress(0), 0);
        this.server.createContext("/image.jpg", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "image/jpeg");
            exchange.sendResponseHeaders(200, jpegBytes.length);
            exchange.getResponseBody().write(jpegBytes);
            exchange.close();
        });
        this.server.start();
        final int port = this.server.getAddress().getPort();
        final String generatedUrl = "http://127.0.0.1:" + port + "/image.jpg";
        when(imageStudio.generateSync(any())).thenReturn(
                new ImageStudioUseCase.GeneratedImageResult(
                        generatedUrl,
                        "image/jpeg",
                        "pollinations",
                        "prompt",
                        null
                )
        );

        service.process(new ProductImageRequestedEvent(
                productId,
                "Dipirona 500mg",
                "Generico",
                "Medicacoes",
                String.valueOf(productId)
        ));

        final ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(produtos).updateImagem(eq(productId), urlCaptor.capture());
        final String persistedUrl = urlCaptor.getValue();

        assertThat(persistedUrl)
                .startsWith("/media/products/produto-7-")
                .endsWith(".png");

        final String filename = persistedUrl.substring(persistedUrl.lastIndexOf('/') + 1);
        final Path savedFile = mediaProductsDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();

        final byte[] signature = Files.readAllBytes(savedFile);
        assertThat(signature[0]).isEqualTo((byte) 0x89);
        assertThat(signature[1]).isEqualTo((byte) 0x50);
        assertThat(signature[2]).isEqualTo((byte) 0x4E);
        assertThat(signature[3]).isEqualTo((byte) 0x47);

        verify(jobs).markRunning(1L);
        verify(jobs).markDone(1L, persistedUrl);
    }

    @Test
    void ensureImageLinkedFromLastSuccessfulJobShouldPersistResultUrl() {
        final ProdutoRepositoryPort produtos = mock(ProdutoRepositoryPort.class);
        final ProductImageJobRepository jobs = mock(ProductImageJobRepository.class);
        final ImageStudioUseCase imageStudio = mock(ImageStudioUseCase.class);
        final ProductPromptFactory promptFactory = new ProductPromptFactory();
        final AiGeneratedImageStorageService generatedImageStorageService =
                mock(AiGeneratedImageStorageService.class);

        final ProductImageJobService service = new ProductImageJobService(
                produtos,
                jobs,
                imageStudio,
                promptFactory,
                generatedImageStorageService
        );

        final Long productId = 25L;
        final String resultUrl = "https://cdn.exemplo.com/produto-25.png";
        when(produtos.findById(productId))
                .thenReturn(Optional.of(new ProdutoRepositoryPort.ProdutoDTO(
                        productId,
                        "Nome",
                        "Desc",
                        "Cat",
                        "789",
                        "Marca",
                        null
                )))
                .thenReturn(Optional.of(new ProdutoRepositoryPort.ProdutoDTO(
                        productId,
                        "Nome",
                        "Desc",
                        "Cat",
                        "789",
                        "Marca",
                        resultUrl
                )));
        when(jobs.findLastByProduct(productId))
                .thenReturn(Optional.of(new ProductImageJobRepository.Job(
                        9L,
                        productId,
                        ProductImageJobRepository.Status.DONE,
                        resultUrl,
                        null,
                        "fp",
                        Instant.now(),
                        Instant.now()
                )));

        final Optional<String> linked = service.ensureImageLinkedFromLastSuccessfulJob(productId);

        assertThat(linked).contains(resultUrl);
        verify(produtos).updateImagem(productId, resultUrl);
    }

    @Test
    void ensureImageLinkedFromLastSuccessfulJobShouldIgnoreNonDoneJob() {
        final ProdutoRepositoryPort produtos = mock(ProdutoRepositoryPort.class);
        final ProductImageJobRepository jobs = mock(ProductImageJobRepository.class);
        final ImageStudioUseCase imageStudio = mock(ImageStudioUseCase.class);
        final ProductPromptFactory promptFactory = new ProductPromptFactory();
        final AiGeneratedImageStorageService generatedImageStorageService =
                mock(AiGeneratedImageStorageService.class);

        final ProductImageJobService service = new ProductImageJobService(
                produtos,
                jobs,
                imageStudio,
                promptFactory,
                generatedImageStorageService
        );

        final Long productId = 30L;
        when(produtos.findById(productId))
                .thenReturn(Optional.of(new ProdutoRepositoryPort.ProdutoDTO(
                        productId,
                        "Nome",
                        "Desc",
                        "Cat",
                        "789",
                        "Marca",
                        null
                )));
        when(jobs.findLastByProduct(productId))
                .thenReturn(Optional.of(new ProductImageJobRepository.Job(
                        10L,
                        productId,
                        ProductImageJobRepository.Status.ERROR,
                        "https://cdn.exemplo.com/falha.png",
                        "erro",
                        "fp2",
                        Instant.now(),
                        Instant.now()
                )));

        final Optional<String> linked = service.ensureImageLinkedFromLastSuccessfulJob(productId);

        assertThat(linked).isEmpty();
        verify(produtos, never()).updateImagem(any(), anyString());
    }

    @Test
    void processShouldDeletePersistedImageWhenRepositoryUpdateFails() throws Exception {
        final ProdutoRepositoryPort produtos = mock(ProdutoRepositoryPort.class);
        final ProductImageJobRepository jobs = mock(ProductImageJobRepository.class);
        final ImageStudioUseCase imageStudio = mock(ImageStudioUseCase.class);
        final ProductPromptFactory promptFactory = new ProductPromptFactory();
        final AiGeneratedImageStorageService generatedImageStorageService =
                mock(AiGeneratedImageStorageService.class);

        final ProductImageJobService service = new ProductImageJobService(
                produtos,
                jobs,
                imageStudio,
                promptFactory,
                generatedImageStorageService
        );

        final Long productId = 7L;
        when(produtos.findById(productId))
                .thenReturn(Optional.of(new ProdutoRepositoryPort.ProdutoDTO(
                        productId,
                        "Dipirona 500mg",
                        "Dipirona 500mg",
                        "Medicacoes",
                        "7890000000000",
                        "Generico",
                        null
                )));
        when(jobs.createQueued(eq(productId), anyString()))
                .thenReturn(new ProductImageJobRepository.Job(
                        1L,
                        productId,
                        ProductImageJobRepository.Status.QUEUED,
                        null,
                        null,
                        "fingerprint",
                        Instant.now(),
                        Instant.now()
                ));
        when(imageStudio.generateSync(any())).thenReturn(
                new ImageStudioUseCase.GeneratedImageResult(
                        "https://cdn.exemplo.com/produto-7.png",
                        "image/png",
                        "openai",
                        "prompt",
                        null
                )
        );
        when(generatedImageStorageService.persistGeneratedImageWithFallback(eq(productId), any()))
                .thenReturn("/media/products/produto-7.png");
        doThrow(new IllegalStateException("db")).when(produtos).updateImagem(productId, "/media/products/produto-7.png");

        service.process(new ProductImageRequestedEvent(
                productId,
                "Dipirona 500mg",
                "Generico",
                "Medicacoes",
                String.valueOf(productId)
        ));

        verify(generatedImageStorageService).deletePersistedProductImage("/media/products/produto-7.png");
        verify(jobs).markError(eq(1L), anyString());
    }

    private static byte[] createSampleJpeg() throws IOException {
        final BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }
}
