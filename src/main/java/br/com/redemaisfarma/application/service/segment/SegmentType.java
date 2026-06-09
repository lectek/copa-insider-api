package br.com.redemaisfarma.application.service.segment;

public enum SegmentType {
    TODOS,
    VIP,
    INATIVOS_90D,
    CATEGORIA,
    RECENCIA,
    TICKET;

    public static SegmentType from(String raw) {
        if (raw == null) {
            return TODOS;
        }
        try {
            return SegmentType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TODOS;
        }
    }
}
