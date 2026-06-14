package br.com.lectek.copainsider.application.copa;

public record GoloVM(
        String jogador,
        String minuto,
        boolean penalty,
        boolean og,
        boolean isCasa
) {
    public int minuteInt() {
        if (minuto == null) return 0;
        String m = minuto.replaceAll("[^0-9+]", "");
        if (m.contains("+")) {
            String[] parts = m.split("\\+");
            try { return Integer.parseInt(parts[0]) * 100 + Integer.parseInt(parts[1]); }
            catch (Exception e) { return 0; }
        }
        try { return Integer.parseInt(m) * 100; } catch (Exception e) { return 0; }
    }

    public String icone() {
        if (og)      return "⚽🔴";
        if (penalty) return "⚽(P)";
        return "⚽";
    }
}
