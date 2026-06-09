package br.com.redemaisfarma.application.core.media;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Configuration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@EnableConfigurationProperties(ImageStorageProperties.class)
public class ImageStorageService {

    private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);
    private static final DateTimeFormatter FILE_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final long MAX_IMAGE_BYTES = 20L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Path TEMP_ROOT = Paths.get(
            System.getProperty("java.io.tmpdir", "tmp")
    ).toAbsolutePath().normalize();

    private final ImageStorageProperties props;
    private final S3Client providedS3Client;
    private volatile S3Client s3Client;

    @Autowired
    public ImageStorageService(ImageStorageProperties props) {
        this(props, null);
    }

    ImageStorageService(ImageStorageProperties props, S3Client s3Client) {
        this.props = props;
        this.providedS3Client = s3Client;
    }

    @PostConstruct
    void logStorageConfigurationRisk() {
        if (props.isS3Provider()) {
            if (!isAbsoluteHttpUrl(props.getPublicBase()) || !isAbsoluteHttpUrl(props.getUserPublicBase())) {
                log.warn(
                        "Storage S3 ativo sem APP_MEDIA_PUBLIC_BASE/APP_MEDIA_USER_PUBLIC_BASE absolutos. "
                                + "As URLs publicas de imagens podem ficar invalidas."
                );
            }
            return;
        }

        logIfTempBacked("produto", props.getDir());
        logIfTempBacked("usuario", props.getUserDir());
    }

    public String saveProductImage(Long productId, MultipartFile file) throws IOException {
        String ext = validateImageUpload(file);
        String fname = buildUniqueFileName("produto", productId, ext);
        byte[] body = file.getBytes();
        String contentType = resolveContentType(file.getContentType(), fname);
        return persistImage(body, contentType, props.getDir(), props.getPublicBase(), props.getS3().getProductPrefix(), fname);
    }

    public String saveUserAvatar(Long userId, MultipartFile file) throws IOException {
        String ext = validateImageUpload(file);
        String fname = buildUniqueFileName("usuario", userId, ext);
        byte[] body = file.getBytes();
        String contentType = resolveContentType(file.getContentType(), fname);
        return persistImage(
                body,
                contentType,
                props.getUserDir(),
                props.getUserPublicBase(),
                props.getS3().getUserPrefix(),
                fname
        );
    }

    public String saveProductImagePng(Long productId, BufferedImage image)
            throws IOException {
        if (image == null) {
            throw new IOException("Imagem invalida para persistencia.");
        }

        String fname = buildUniqueFileName("produto", productId, ".png");
        byte[] body;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", out)) {
                throw new IOException("Falha ao converter imagem para PNG.");
            }
            body = out.toByteArray();
        }
        return persistImage(body, "image/png", props.getDir(), props.getPublicBase(), props.getS3().getProductPrefix(), fname);
    }

    public void deleteProductImageByUrl(String imageUrl) {
        deleteStoredImage(imageUrl, props.getPublicBase(), props.getDir(), props.getS3().getProductPrefix());
    }

    public void deleteUserAvatarByUrl(String imageUrl) {
        deleteStoredImage(imageUrl, props.getUserPublicBase(), props.getUserDir(), props.getS3().getUserPrefix());
    }

    private static String extractExtension(String name) {
        int i = name.lastIndexOf('.');
        String e = i >= 0 ? name.substring(i) : "";
        if (e.length() > 8 || e.contains("/") || e.contains("\\")) e = "";
        return e.toLowerCase(Locale.ROOT);
    }

    private static String joinPublic(String base, String file) {
        String b = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
        String f = file.startsWith("/") ? file.substring(1) : file;
        return b + "/" + f;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isAllowedImageType(String contentType) {
        String normalized = contentType.toLowerCase(Locale.ROOT);
        return normalized.equals("image/jpeg")
                || normalized.equals("image/jpg")
                || normalized.equals("image/pjpeg")
                || normalized.equals("image/png")
                || normalized.equals("image/webp");
    }

    private static boolean isAllowedImageExtension(String extension) {
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private static String sanitizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String normalized = contentType.toLowerCase(Locale.ROOT).trim();
        int sep = normalized.indexOf(';');
        if (sep >= 0) {
            normalized = normalized.substring(0, sep).trim();
        }
        return normalized;
    }

    private String validateImageUpload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("Arquivo vazio");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new IOException("Imagem acima de 20MB");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "image" : file.getOriginalFilename());
        String ext = extractExtension(original);
        boolean extensionAllowed = isAllowedImageExtension(ext);

        String ctype = sanitizeContentType(file.getContentType());
        boolean contentTypeAllowed = !ctype.isBlank() && isAllowedImageType(ctype);

        if (!contentTypeAllowed && !extensionAllowed) {
            throw new IOException("Tipo de arquivo invalido (somente PNG, JPG ou WEBP)");
        }

        if (!contentTypeAllowed && extensionAllowed) {
            log.debug(
                    "Upload aceito por extensao valida apesar do Content-Type '{}': arquivo '{}'",
                    ctype.isBlank() ? "<vazio>" : ctype,
                    original
            );
        }

        return ext;
    }

    private static Path resolveBaseDir(String dir) throws IOException {
        Path base = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(base);
        return base;
    }

    private static String buildUniqueFileName(String prefix, Long entityId, String extension) {
        String ts = LocalDateTime.now().format(FILE_TIMESTAMP_FORMAT);
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return prefix
                + "-"
                + (entityId == null ? "na" : entityId)
                + "-"
                + ts
                + "-"
                + token
                + extension;
    }

    private String persistImage(
            byte[] body,
            String contentType,
            String baseDir,
            String publicBase,
            String s3Prefix,
            String fileName
    ) throws IOException {
        if (props.isS3Provider()) {
            return saveToS3(body, contentType, publicBase, s3Prefix, fileName);
        }
        return saveToLocal(body, baseDir, publicBase, fileName);
    }

    private String saveToLocal(byte[] body, String baseDir, String publicBase, String fileName) throws IOException {
        Path base = resolveBaseDir(baseDir);
        Path target = base.resolve(fileName);
        Files.write(target, body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return joinPublic(publicBase, fileName);
    }

    private String saveToS3(
            byte[] body,
            String contentType,
            String publicBase,
            String configuredPrefix,
            String fileName
    ) throws IOException {
        ImageStorageProperties.S3 s3 = validateS3Configuration();
        String resolvedPublicBase = resolveAbsolutePublicBase(publicBase);
        String key = buildS3Key(configuredPrefix, fileName);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(trimToNull(s3.getBucket()))
                .key(key)
                .contentLength((long) body.length)
                .contentType(resolveContentType(contentType, fileName))
                .cacheControl("public, max-age=31536000")
                .build();
        try {
            s3Client().putObject(request, RequestBody.fromBytes(body));
        } catch (S3Exception | SdkClientException ex) {
            throw new IOException("Falha ao enviar imagem para storage persistente.", ex);
        }
        return joinPublic(resolvedPublicBase, fileName);
    }

    private void deleteStoredImage(String imageUrl, String publicBase, String baseDir, String configuredPrefix) {
        if (props.isS3Provider()) {
            deleteFromS3(imageUrl, publicBase, configuredPrefix);
            return;
        }
        deleteFromLocal(imageUrl, publicBase, baseDir);
    }

    private void deleteFromLocal(String imageUrl, String publicBase, String baseDir) {
        String fileName = resolveStoredFileName(imageUrl, publicBase);
        if (fileName == null) {
            return;
        }
        try {
            Path base = Paths.get(baseDir).toAbsolutePath().normalize();
            Path target = base.resolve(fileName).normalize();
            if (!target.startsWith(base)) {
                log.warn("Ignorando exclusao de imagem fora do diretorio configurado: {}", imageUrl);
                return;
            }
            Files.deleteIfExists(target);
        } catch (IOException | InvalidPathException ex) {
            log.warn("Falha ao remover imagem persistida '{}': {}", imageUrl, ex.getMessage());
        }
    }

    private void deleteFromS3(String imageUrl, String publicBase, String configuredPrefix) {
        String resolvedPublicBase;
        try {
            resolvedPublicBase = resolveAbsolutePublicBase(publicBase);
        } catch (IllegalStateException ex) {
            log.warn("Falha ao resolver base publica do storage S3 para exclusao: {}", ex.getMessage());
            return;
        }
        String fileName = resolveStoredFileName(imageUrl, resolvedPublicBase);
        if (fileName == null) {
            return;
        }
        String key = buildS3Key(configuredPrefix, fileName);
        try {
            s3Client().deleteObject(DeleteObjectRequest.builder()
                    .bucket(trimToNull(props.getS3().getBucket()))
                    .key(key)
                    .build());
        } catch (S3Exception | SdkClientException ex) {
            log.warn("Falha ao remover imagem persistida '{}' do storage S3: {}", imageUrl, ex.getMessage());
        }
    }

    private static String resolveStoredFileName(String imageUrl, String publicBase) {
        if (imageUrl == null || imageUrl.isBlank() || publicBase == null || publicBase.isBlank()) {
            return null;
        }
        String normalizedUrl = sanitizePublicPath(imageUrl);
        String normalizedBase = sanitizePublicPath(publicBase);
        if (normalizedUrl == null || normalizedBase == null) {
            return null;
        }

        String prefix = normalizedBase.endsWith("/") ? normalizedBase : normalizedBase + "/";
        if (!normalizedUrl.startsWith(prefix)) {
            return null;
        }
        String candidate = normalizedUrl.substring(prefix.length());
        if (candidate.isBlank() || candidate.contains("/") || candidate.contains("\\")) {
            return null;
        }
        String clean = StringUtils.cleanPath(candidate);
        if (clean.contains("..")) {
            return null;
        }
        return clean;
    }

    private static String sanitizePublicPath(String value) {
        String normalized = value == null ? null : value.trim();
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized.replace('\\', '/');
    }

    private static boolean isAbsoluteHttpUrl(String value) {
        String normalized = trimToNull(value);
        return normalized != null
                && (normalized.startsWith("http://") || normalized.startsWith("https://"));
    }

    private String resolveAbsolutePublicBase(String configuredPublicBase) {
        String normalized = trimToNull(configuredPublicBase);
        if (!isAbsoluteHttpUrl(normalized)) {
            throw new IllegalStateException(
                    "Defina APP_MEDIA_PUBLIC_BASE e APP_MEDIA_USER_PUBLIC_BASE com URL absoluta quando APP_MEDIA_PROVIDER=s3."
            );
        }
        return normalized;
    }

    private ImageStorageProperties.S3 validateS3Configuration() {
        ImageStorageProperties.S3 s3 = props.getS3();
        if (trimToNull(s3.getBucket()) == null) {
            throw new IllegalStateException("APP_MEDIA_S3_BUCKET e obrigatorio quando APP_MEDIA_PROVIDER=s3.");
        }
        boolean accessKeyPresent = trimToNull(s3.getAccessKey()) != null;
        boolean secretKeyPresent = trimToNull(s3.getSecretKey()) != null;
        if (accessKeyPresent != secretKeyPresent) {
            throw new IllegalStateException(
                    "APP_MEDIA_S3_ACCESS_KEY e APP_MEDIA_S3_SECRET_KEY devem ser informados juntos."
            );
        }
        return s3;
    }

    private S3Client s3Client() {
        if (providedS3Client != null) {
            return providedS3Client;
        }
        S3Client current = s3Client;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (s3Client == null) {
                s3Client = buildS3Client();
            }
            return s3Client;
        }
    }

    private S3Client buildS3Client() {
        ImageStorageProperties.S3 s3 = validateS3Configuration();
        S3ClientBuilder builder = S3Client.builder()
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .region(Region.of(trimToNull(s3.getRegion()) == null ? "us-east-1" : trimToNull(s3.getRegion())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3.isPathStyleAccess())
                        .build());

        String endpoint = trimToNull(s3.getEndpoint());
        if (endpoint != null) {
            builder.endpointOverride(URI.create(endpoint));
        }

        String accessKey = trimToNull(s3.getAccessKey());
        String secretKey = trimToNull(s3.getSecretKey());
        if (accessKey != null && secretKey != null) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            );
        }

        return builder.build();
    }

    private static String buildS3Key(String configuredPrefix, String fileName) {
        String prefix = trimToNull(configuredPrefix);
        if (prefix == null) {
            return fileName;
        }
        String normalizedPrefix = prefix.replace('\\', '/');
        while (normalizedPrefix.startsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(1);
        }
        while (normalizedPrefix.endsWith("/")) {
            normalizedPrefix = normalizedPrefix.substring(0, normalizedPrefix.length() - 1);
        }
        return normalizedPrefix.isEmpty() ? fileName : normalizedPrefix + "/" + fileName;
    }

    private static String resolveContentType(String contentType, String fileName) {
        String sanitized = sanitizeContentType(contentType);
        if (!sanitized.isBlank() && isAllowedImageType(sanitized)) {
            return sanitized;
        }
        String ext = extractExtension(fileName);
        return switch (ext) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".webp" -> "image/webp";
            default -> "image/png";
        };
    }

    private void logIfTempBacked(String label, String configuredDir) {
        String normalized = trimToNull(configuredDir);
        if (normalized == null) {
            return;
        }
        try {
            Path candidate = Paths.get(normalized).toAbsolutePath().normalize();
            if (candidate.startsWith(TEMP_ROOT)) {
                log.warn(
                        "Storage local de {} esta em diretorio temporario ({}). Deploy/restart pode apagar imagens. "
                                + "Use volume persistente ou APP_MEDIA_PROVIDER=s3 em producao.",
                        label,
                        candidate
                );
            }
        } catch (InvalidPathException ex) {
            log.warn("Diretorio de storage invalido para {}: {}", label, configuredDir);
        }
    }
}
