package br.com.redemaisfarma.application.service;

import br.com.redemaisfarma.application.core.media.ImageStorageProperties;
import br.com.redemaisfarma.application.core.media.ImageStorageService;
import br.com.redemaisfarma.application.port.inbound.ImageStudioUseCase;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class AiGeneratedImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPersistBase64DataUriAsPng() throws Exception {
        final ImageStorageProperties props = new ImageStorageProperties();
        final Path mediaProductsDir = tempDir.resolve("media").resolve("products");
        props.setDir(mediaProductsDir.toString());
        props.setPublicBase("/media/products");

        final ImageStorageService imageStorageService = new ImageStorageService(props);
        final AiGeneratedImageStorageService storageService =
                new AiGeneratedImageStorageService(imageStorageService);

        final String storedUrl = storageService.persistGeneratedImage(
                99L,
                new ImageStudioUseCase.GeneratedImageResult(
                        buildSamplePngDataUri(),
                        "image/png",
                        "openai",
                        "prompt",
                        null
                )
        );

        assertThat(storedUrl)
                .startsWith("/media/products/produto-99-")
                .endsWith(".png");

        final String filename = storedUrl.substring(storedUrl.lastIndexOf('/') + 1);
        final Path savedFile = mediaProductsDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();
        final byte[] signature = Files.readAllBytes(savedFile);
        assertThat(signature[0]).isEqualTo((byte) 0x89);
        assertThat(signature[1]).isEqualTo((byte) 0x50);
        assertThat(signature[2]).isEqualTo((byte) 0x4E);
        assertThat(signature[3]).isEqualTo((byte) 0x47);
    }

    private static String buildSamplePngDataUri() throws IOException {
        final BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFFFFFF);
        image.setRGB(1, 0, 0xDDDDDD);
        image.setRGB(0, 1, 0xBBBBBB);
        image.setRGB(1, 1, 0x999999);

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
    }
}
