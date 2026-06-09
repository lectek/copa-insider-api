package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class AiGeneratedImageStorageService {

    private static final Duration HTTP_CONNECT_TIMEOUT = Duration.ofSeconds(12);
    private static final Duration HTTP_REQUEST_TIMEOUT = Duration.ofSeconds(25);
    private static final Duration HTTP_RETRY_BASE_DELAY = Duration.ofMillis(700);
    private static final int HTTP_DOWNLOAD_MAX_ATTEMPTS = 3;
    private static final String HTTP_ERROR_PREFIX = "Falha ao baixar imagem da IA. HTTP ";
    private static final String IMAGE_FETCH_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
    private static final Set<Integer> RETRYABLE_HTTP_STATUS = Set.of(
            408, 425, 429, 500, 502, 503, 504, 520, 521, 522, 523, 524, 525, 526, 527, 529, 530
    );

    private final ImageStorageService imageStorageService;
    private final HttpClient httpClient;

    public AiGeneratedImageStorageService(final ImageStorageService imageStorageService) {
        this.imageStorageService = imageStorageService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(HTTP_CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String persistGeneratedImage(
            final Long productId,
            final ImageStudioUseCase.GeneratedImageResult generatedImage
    ) throws IOException {
        final byte[] body = resolveBody(generatedImage);
        if (body == null || body.length == 0) {
            throw new IOException("Conteudo da imagem gerada esta vazio.");
        }

        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(body));
        if (image == null) {
            throw new IOException("Formato da imagem gerada nao e suportado para conversao.");
        }

        return this.imageStorageService.saveProductImagePng(productId, image);
    }

    public String persistGeneratedImageWithFallback(
            final Long productId,
            final ImageStudioUseCase.GeneratedImageResult generatedImage
    ) throws IOException {
        try {
            return persistGeneratedImage(productId, generatedImage);
        } catch (IOException ex) {
            if (canFallbackToRemoteUrl(generatedImage, ex)) {
                return generatedImage.reference().trim();
            }
            throw ex;
        }
    }

    public void deletePersistedProductImage(final String imageUrl) {
        this.imageStorageService.deleteProductImageByUrl(imageUrl);
    }

    private byte[] resolveBody(final ImageStudioUseCase.GeneratedImageResult generatedImage)
            throws IOException {
        final String reference = generatedImage == null ? null : trimToNull(generatedImage.reference());
        if (reference == null) {
            throw new IOException("Referencia da imagem gerada esta vazia.");
        }
        if (reference.startsWith("data:")) {
            return decodeDataUri(reference);
        }

        final URI imageUri;
        try {
            imageUri = URI.create(reference);
        } catch (IllegalArgumentException ex) {
            throw new IOException("URL gerada da imagem e invalida.", ex);
        }
        return downloadImageBodyWithRetry(imageUri);
    }

    private byte[] decodeDataUri(final String dataUri) throws IOException {
        final int commaIndex = dataUri.indexOf(',');
        if (commaIndex < 0) {
            throw new IOException("Data URI da imagem esta invalida.");
        }
        final String metadata = dataUri.substring(0, commaIndex).toLowerCase();
        if (!metadata.contains(";base64")) {
            throw new IOException("Data URI da imagem nao esta em base64.");
        }
        final String encoded = dataUri.substring(commaIndex + 1).trim();
        if (encoded.isEmpty()) {
            throw new IOException("Conteudo base64 da imagem gerada esta vazio.");
        }
        try {
            return Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException ex) {
            throw new IOException("Base64 da imagem gerada esta invalido.", ex);
        }
    }

    private byte[] downloadImageBodyWithRetry(final URI imageUri) throws IOException {
        IOException lastIo = null;
        int lastStatus = -1;

        for (int attempt = 1; attempt <= HTTP_DOWNLOAD_MAX_ATTEMPTS; attempt++) {
            final HttpRequest request = HttpRequest.newBuilder(imageUri)
                    .GET()
                    .timeout(HTTP_REQUEST_TIMEOUT)
                    .header("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")
                    .header("User-Agent", IMAGE_FETCH_USER_AGENT)
                    .build();

            final HttpResponse<byte[]> response;
            try {
                response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException("Download da imagem foi interrompido.", ex);
            } catch (IOException ex) {
                lastIo = ex;
                if (attempt < HTTP_DOWNLOAD_MAX_ATTEMPTS) {
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw ex;
            }

            final int status = response.statusCode();
            if (status >= 200 && status < 300) {
                return response.body();
            }

            lastStatus = status;
            if (attempt < HTTP_DOWNLOAD_MAX_ATTEMPTS && RETRYABLE_HTTP_STATUS.contains(status)) {
                sleepBeforeRetry(attempt);
                continue;
            }
            break;
        }

        if (lastStatus > 0) {
            throw new IOException(HTTP_ERROR_PREFIX + lastStatus);
        }
        throw new IOException("Falha ao baixar imagem da IA.", lastIo);
    }

    private static void sleepBeforeRetry(final int attempt) throws IOException {
        final long delayMs = HTTP_RETRY_BASE_DELAY.toMillis() * attempt;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Retry de download foi interrompido.", ex);
        }
    }

    private static boolean canFallbackToRemoteUrl(
            final ImageStudioUseCase.GeneratedImageResult generatedImage,
            final IOException ex
    ) {
        final String reference = generatedImage == null ? null : trimToNull(generatedImage.reference());
        if (reference == null) {
            return false;
        }
        if (!(reference.startsWith("http://") || reference.startsWith("https://"))) {
            return false;
        }
        final String message = ex.getMessage() == null ? "" : ex.getMessage();
        return message.startsWith(HTTP_ERROR_PREFIX)
                || message.contains("Conteudo da imagem gerada esta vazio")
                || message.contains("Formato da imagem gerada nao e suportado");
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
