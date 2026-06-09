package br.com.redemaisfarma.application.core.media;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void saveProductImageAcceptsValidExtensionWithGenericContentType() throws Exception {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setDir(tempDir.resolve("media/products").toString());
        props.setPublicBase("/media/products");

        ImageStorageService service = new ImageStorageService(props);
        MockMultipartFile file = new MockMultipartFile(
                "imagemFile",
                "foto.JPG",
                "application/octet-stream",
                new byte[]{1, 2, 3, 4}
        );

        String url = service.saveProductImage(42L, file);

        assertThat(url).startsWith("/media/products/produto-42-").endsWith(".jpg");
        Path savedFile = Paths.get(props.getDir()).resolve(fileNameFromUrl(url));
        assertThat(savedFile).exists();
    }

    @Test
    void saveProductImageRejectsInvalidContentTypeAndExtension() {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setDir(tempDir.resolve("media/products").toString());
        props.setPublicBase("/media/products");

        ImageStorageService service = new ImageStorageService(props);
        MockMultipartFile file = new MockMultipartFile(
                "imagemFile",
                "arquivo.txt",
                "application/pdf",
                "not-an-image".getBytes(StandardCharsets.UTF_8)
        );

        assertThatThrownBy(() -> service.saveProductImage(7L, file))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Tipo de arquivo invalido");
    }

    @Test
    void saveProductImageGeneratesDistinctUrlsForSequentialUploads() throws Exception {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setDir(tempDir.resolve("media/products").toString());
        props.setPublicBase("/media/products");

        ImageStorageService service = new ImageStorageService(props);
        MockMultipartFile file1 = new MockMultipartFile(
                "imagemFile",
                "foto.png",
                "image/png",
                new byte[]{1, 2, 3}
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "imagemFile",
                "foto.png",
                "image/png",
                new byte[]{4, 5, 6}
        );

        String firstUrl = service.saveProductImage(42L, file1);
        String secondUrl = service.saveProductImage(42L, file2);

        assertThat(firstUrl).isNotEqualTo(secondUrl);
        assertThat(Paths.get(props.getDir()).resolve(fileNameFromUrl(firstUrl))).exists();
        assertThat(Paths.get(props.getDir()).resolve(fileNameFromUrl(secondUrl))).exists();
    }

    @Test
    void deleteProductImageByUrlRemovesPersistedFile() throws Exception {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setDir(tempDir.resolve("media/products").toString());
        props.setPublicBase("/media/products");

        ImageStorageService service = new ImageStorageService(props);
        MockMultipartFile file = new MockMultipartFile(
                "imagemFile",
                "foto.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        String url = service.saveProductImage(42L, file);
        Path savedFile = Paths.get(props.getDir()).resolve(fileNameFromUrl(url));
        assertThat(savedFile).exists();

        service.deleteProductImageByUrl(url);

        assertThat(savedFile).doesNotExist();
    }

    @Test
    void saveUserAvatarUsesS3AndReturnsConfiguredPublicUrl() throws Exception {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setProvider("s3");
        props.setUserPublicBase("https://cdn.example.com/users");
        props.getS3().setBucket("redemais-media");
        props.getS3().setUserPrefix("avatars");

        S3Client s3Client = mock(S3Client.class);
        ImageStorageService service = new ImageStorageService(props, s3Client);
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "perfil.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        String url = service.saveUserAvatar(99L, file);

        org.mockito.ArgumentCaptor<PutObjectRequest> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("redemais-media");
        assertThat(request.key()).startsWith("avatars/usuario-99-").endsWith(".png");
        assertThat(url).startsWith("https://cdn.example.com/users/usuario-99-").endsWith(".png");
    }

    @Test
    void deleteUserAvatarByUrlRemovesObjectFromS3() {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setProvider("s3");
        props.setUserPublicBase("https://cdn.example.com/users");
        props.getS3().setBucket("redemais-media");
        props.getS3().setUserPrefix("avatars");

        S3Client s3Client = mock(S3Client.class);
        ImageStorageService service = new ImageStorageService(props, s3Client);

        service.deleteUserAvatarByUrl("https://cdn.example.com/users/avatar-final.png");

        org.mockito.ArgumentCaptor<DeleteObjectRequest> requestCaptor =
                org.mockito.ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(requestCaptor.capture());

        DeleteObjectRequest request = requestCaptor.getValue();
        assertThat(request.bucket()).isEqualTo("redemais-media");
        assertThat(request.key()).isEqualTo("avatars/avatar-final.png");
    }

    @Test
    void saveProductImageInS3RequiresAbsolutePublicBase() {
        ImageStorageProperties props = new ImageStorageProperties();
        props.setProvider("s3");
        props.setPublicBase("/media/products");
        props.getS3().setBucket("redemais-media");
        props.getS3().setProductPrefix("products");

        S3Client s3Client = mock(S3Client.class);
        ImageStorageService service = new ImageStorageService(props, s3Client);
        MockMultipartFile file = new MockMultipartFile(
                "imagemFile",
                "foto.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> service.saveProductImage(1L, file))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_MEDIA_PUBLIC_BASE");
    }

    private static String fileNameFromUrl(String url) {
        int idx = url.lastIndexOf('/');
        return idx >= 0 ? url.substring(idx + 1) : url;
    }
}
