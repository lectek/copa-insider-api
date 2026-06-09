package br.com.lectek.copainsider.adapters.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ImageGenRequestDTO(
        @NotBlank @Size(max = 64) @JsonProperty("preset")
        String preset,

        @Size(max = 800) @JsonProperty("prompt")
        String prompt,

        @JsonProperty("vars")
        Map<String, Object> vars,

        @Size(max = 2048) @JsonProperty("inputImageUrl")
        String inputImageUrl,

        @JsonProperty("removeBackground")
        boolean removeBackground,

        @JsonProperty("upscale")
        boolean upscale
) {
    /** Retorna uma lista segura de cores (somente Strings). */
    public List<String> cores() {
        if (vars == null) return List.of();
        Object v = vars.get("cores");
        if (v instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }

    /** Lê uma chave textual de {@code vars} com default. */
    public String varText(String key, String def) {
        if (vars == null) return def;
        Object v = vars.get(key);
        if (v instanceof String s && !s.isBlank()) return s;
        return def;
    }

    public enum Preset {
        packshot, banner_feed, banner_story, carrossel;

        public static boolean isValid(String p) {
            try {
                Preset.valueOf(p);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
