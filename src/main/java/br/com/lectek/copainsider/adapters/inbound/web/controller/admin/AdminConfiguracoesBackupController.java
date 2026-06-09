package br.com.lectek.copainsider.adapters.inbound.web.controller.admin;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.AppSettingEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.repository.AppSettingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/configuracoes/backup")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfiguracoesBackupController {

    /**
     * Local directory used to store JSON backups.
     */
    private static final Path BACKUP_DIR = Paths.get("storage", "backups");

    /**
     * Date pattern used in generated file names.
     */
    private static final DateTimeFormatter FILE_TS =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /**
     * Repository used to read and write application settings.
     */
    private final AppSettingRepository repository;

    /**
     * JSON serializer/deserializer for backup payload.
     */
    private final ObjectMapper objectMapper;

    /**
     * Creates backup controller dependencies.
     *
     * @param settingsRepository settings repository
     * @param mapper object mapper
     */
    public AdminConfiguracoesBackupController(
            final AppSettingRepository settingsRepository,
            final ObjectMapper mapper
    ) {
        this.repository = settingsRepository;
        this.objectMapper = mapper;
    }

    /**
     * Renders backup management page.
     *
     * @param model view model
     * @return backup settings page
     */
    @GetMapping
    public String form(final Model model) {
        model.addAttribute("backups", listBackups());
        return "pages/admin/configuracoes/backup";
    }

    /**
     * Generates a new backup file with current settings.
     *
     * @param ra redirect attributes
     * @return redirect to backup page
     */
    @PostMapping("/now")
    public String gerarBackup(final RedirectAttributes ra) {
        try {
            Files.createDirectories(BACKUP_DIR);

            final LocalDateTime now = LocalDateTime.now();
            final BackupPayload payload = new BackupPayload();
            payload.setGeradoEm(now);
            payload.setSettings(repository.findAll());

            final String filename = "backup-" + FILE_TS.format(now) + ".json";
            final Path target = BACKUP_DIR.resolve(filename);
            final String json = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payload);
            Files.writeString(target, json, StandardCharsets.UTF_8);
            ra.addFlashAttribute("success", "Backup gerado: " + filename);
        } catch (final Exception ex) {
            ra.addFlashAttribute("error", "Falha ao gerar backup.");
        }
        return "redirect:/admin/configuracoes/backup";
    }

    /**
     * Restores settings from an existing backup file.
     *
     * @param id backup file id
     * @param ra redirect attributes
     * @return redirect to backup page
     */
    @PostMapping("/restore/{id}")
    public String restaurar(
            @PathVariable("id") final String id,
            final RedirectAttributes ra
    ) {
        final Path target = safeResolve(id);
        if (target == null || !Files.exists(target)) {
            ra.addFlashAttribute("error", "Backup nao encontrado.");
            return "redirect:/admin/configuracoes/backup";
        }

        try (InputStream in = Files.newInputStream(target)) {
            final BackupPayload payload = objectMapper.readValue(
                    in,
                    BackupPayload.class
            );
            if (payload != null && payload.getSettings() != null) {
                for (final AppSettingEntity entity : payload.getSettings()) {
                    upsertSetting(entity);
                }
            }
            ra.addFlashAttribute("success", "Backup restaurado.");
        } catch (final Exception ex) {
            ra.addFlashAttribute("error", "Falha ao restaurar backup.");
        }
        return "redirect:/admin/configuracoes/backup";
    }

    /**
     * Downloads a backup file by id.
     *
     * @param id backup file id
     * @return file response
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> baixar(
            @PathVariable("id") final String id
    ) {
        final Path target = safeResolve(id);
        if (target == null || !Files.exists(target)) {
            return ResponseEntity.notFound().build();
        }

        try {
            final InputStreamResource resource = new InputStreamResource(
                    Files.newInputStream(target)
            );
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + target.getFileName()
                                    + "\""
                    )
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (final IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private void upsertSetting(final AppSettingEntity entity) {
        if (entity.getSettingKey() == null) {
            return;
        }

        repository.findBySettingKey(entity.getSettingKey())
                .map(existing -> {
                    existing.setSettingValue(entity.getSettingValue());
                    existing.setDescription(entity.getDescription());
                    return repository.save(existing);
                })
                .orElseGet(() -> repository.save(new AppSettingEntity(
                        entity.getSettingKey(),
                        entity.getSettingValue(),
                        entity.getDescription()
                )));
    }

    private List<BackupItem> listBackups() {
        if (!Files.exists(BACKUP_DIR)) {
            return List.of();
        }

        final List<BackupItem> items = new ArrayList<>();
        try (var stream = Files.list(BACKUP_DIR)) {
            stream.filter(Files::isRegularFile)
                    .sorted(
                            Comparator.comparingLong(this::safeLastModified)
                                    .reversed()
                    )
                    .forEach(path -> items.add(new BackupItem(
                            path.getFileName().toString(),
                            formatDate(path)
                    )));
        } catch (final Exception ignored) {
            return List.of();
        }
        return items;
    }

    private long safeLastModified(final Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (final Exception ex) {
            return 0L;
        }
    }

    private ZonedDateTime formatDate(final Path path) {
        try {
            return ZonedDateTime.ofInstant(
                    Instant.ofEpochMilli(safeLastModified(path)),
                    ZoneId.systemDefault()
            );
        } catch (final Exception ex) {
            return ZonedDateTime.now();
        }
    }

    private Path safeResolve(final String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        final Path candidate = BACKUP_DIR.resolve(
                Paths.get(id).getFileName()
        ).normalize();
        if (!candidate.startsWith(BACKUP_DIR)) {
            return null;
        }
        return candidate;
    }

    @Getter
    @AllArgsConstructor
    public static final class BackupItem {

        /**
         * Backup file name.
         */
        private final String nome;

        /**
         * Backup file date.
         */
        private final ZonedDateTime data;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static final class BackupPayload {

        /**
         * Timestamp when backup was generated.
         */
        private LocalDateTime geradoEm;

        /**
         * Settings snapshot.
         */
        private List<AppSettingEntity> settings;
    }
}
