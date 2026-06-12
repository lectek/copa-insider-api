package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SalaVotoJogadorEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.SalaVotoJogadorJPARepository;
import br.com.lectek.copainsider.application.copa.JogoConvocado;
import br.com.lectek.copainsider.application.copa.SelecaoLineupsData;
import br.com.lectek.copainsider.application.copa.vm.NotaJogadorVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class NotasJogadorService {

    static final int VOTOS_MINIMO_PUBLICO = 10;

    private final SalaVotoJogadorJPARepository repo;
    private final SelecaoLineupsData lineups;
    private final SalaJogoSseRegistry sse;
    private final ObjectMapper json;

    public NotasJogadorService(SalaVotoJogadorJPARepository repo,
                               SelecaoLineupsData lineups,
                               SalaJogoSseRegistry sse,
                               ObjectMapper json) {
        this.repo    = repo;
        this.lineups = lineups;
        this.sse     = sse;
        this.json    = json;
    }

    public List<NotaJogadorVM> estado(Long jogoId, String selecaoSlug, String sessionId) {
        List<JogoConvocado> squad = lineups.getLineup(selecaoSlug);
        if (squad.isEmpty()) return List.of();

        Map<String, double[]> medias = calcularMedias(jogoId);
        Map<String, Integer> meuVoto = meuVotoAtual(jogoId, sessionId, squad);

        return squad.stream().map(p -> {
            double[] mv = medias.getOrDefault(p.slug(), new double[]{0, 0});
            int total   = (int) mv[1];
            double med  = total > 0 ? mv[0] : 0;
            return new NotaJogadorVM(p.slug(), p.nome(), p.posicao(), p.posicaoLabel(),
                    p.numero(), p.foto(), p.selecaoSlug(),
                    total >= VOTOS_MINIMO_PUBLICO ? Math.round(med * 10.0) / 10.0 : 0,
                    total,
                    total >= VOTOS_MINIMO_PUBLICO,
                    meuVoto.get(p.slug()));
        }).toList();
    }

    public NotaJogadorVM votar(Long jogoId, String selecaoSlug, String jogadorSlug,
                               String sessionId, int nota) {
        if (nota < 1 || nota > 10) throw new IllegalArgumentException("Nota inválida");

        JogoConvocado conv = lineups.getLineup(selecaoSlug).stream()
                .filter(j -> j.slug().equals(jogadorSlug))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Jogador não encontrado"));

        // upsert: se já votou, atualiza; se não, cria
        Optional<SalaVotoJogadorEntity> existing =
                repo.findByJogoIdAndSessionIdAndJogadorSlug(jogoId, sessionId, jogadorSlug);
        SalaVotoJogadorEntity entity = existing.orElseGet(() -> {
            SalaVotoJogadorEntity e = new SalaVotoJogadorEntity();
            e.setJogoId(jogoId);
            e.setSessionId(sessionId);
            e.setJogadorSlug(jogadorSlug);
            e.setSelecaoSlug(selecaoSlug);
            return e;
        });
        entity.setNota(nota);
        try {
            repo.save(entity);
        } catch (DataIntegrityViolationException e) {
            // concurrent insert — silently ignore
        }

        Object[] agg = repo.calcularMediaJogador(jogoId, jogadorSlug);
        double media = agg[0] != null ? ((Number) agg[0]).doubleValue() : nota;
        int total    = agg[1] != null ? ((Number) agg[1]).intValue() : 1;

        NotaJogadorVM vm = new NotaJogadorVM(
                jogadorSlug, conv.nome(), conv.posicao(), conv.posicaoLabel(),
                conv.numero(), conv.foto(), selecaoSlug,
                total >= VOTOS_MINIMO_PUBLICO ? Math.round(media * 10.0) / 10.0 : 0,
                total, total >= VOTOS_MINIMO_PUBLICO, nota);

        try {
            sse.broadcastNotas(jogoId, json.writeValueAsString(Map.of(
                "slug", jogadorSlug,
                "media", vm.media(),
                "totalVotos", vm.totalVotos(),
                "votoMinimo", vm.votoMinimo()
            )));
        } catch (Exception ignored) {}
        return vm;
    }

    private Map<String, double[]> calcularMedias(Long jogoId) {
        Map<String, double[]> map = new HashMap<>();
        for (Object[] row : repo.calcularMediasPorJogo(jogoId)) {
            String slug = (String) row[0];
            double med  = row[1] != null ? ((Number) row[1]).doubleValue() : 0;
            long cnt    = row[2] != null ? ((Number) row[2]).longValue() : 0;
            map.put(slug, new double[]{med, cnt});
        }
        return map;
    }

    private Map<String, Integer> meuVotoAtual(Long jogoId, String sessionId, List<JogoConvocado> squad) {
        Map<String, Integer> map = new HashMap<>();
        if (sessionId == null) return map;
        for (JogoConvocado p : squad) {
            repo.findByJogoIdAndSessionIdAndJogadorSlug(jogoId, sessionId, p.slug())
                .ifPresent(v -> map.put(p.slug(), v.getNota()));
        }
        return map;
    }
}
