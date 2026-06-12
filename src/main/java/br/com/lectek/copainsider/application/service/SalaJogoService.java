package br.com.lectek.copainsider.application.service;

import br.com.lectek.copainsider.adapters.outbound.persistence.entity.SalaChatMensagemEntity;
import br.com.lectek.copainsider.adapters.outbound.persistence.jpa.SalaChatMensagemJPARepository;
import br.com.lectek.copainsider.application.copa.vm.ChatMensagemVM;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class SalaJogoService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_MENSAGEM = 300;

    private final SalaChatMensagemJPARepository repo;
    private final SalaJogoSseRegistry sse;
    private final ObjectMapper json;

    public SalaJogoService(SalaChatMensagemJPARepository repo,
                           SalaJogoSseRegistry sse,
                           ObjectMapper json) {
        this.repo = repo;
        this.sse  = sse;
        this.json = json;
    }

    public List<ChatMensagemVM> historico(Long jogoId) {
        return repo.findTop100ByJogoIdAndAtivoTrueOrderByCriadoEmAsc(jogoId)
                   .stream().map(this::toVM).toList();
    }

    public ChatMensagemVM enviar(Long jogoId, String sessionId, String autorNome, String mensagem) {
        if (autorNome == null || autorNome.isBlank()) throw new IllegalArgumentException("Nome obrigatório");
        String nome = autorNome.trim().substring(0, Math.min(autorNome.trim().length(), 40));
        String texto = mensagem == null ? "" : mensagem.trim().substring(0, Math.min(mensagem.trim().length(), MAX_MENSAGEM));
        if (texto.isBlank()) throw new IllegalArgumentException("Mensagem vazia");

        SalaChatMensagemEntity e = new SalaChatMensagemEntity();
        e.setJogoId(jogoId);
        e.setSessionId(sessionId);
        e.setAutorNome(nome);
        e.setMensagem(texto);
        repo.save(e);

        ChatMensagemVM vm = toVM(e);
        try {
            sse.broadcastChat(jogoId, json.writeValueAsString(Map.of(
                "tipo", "MENSAGEM",
                "id", vm.id(),
                "autorNome", vm.autorNome(),
                "mensagem", vm.mensagem(),
                "criadoEm", vm.criadoEm()
            )));
        } catch (Exception ignored) {}
        return vm;
    }

    public void reagir(Long jogoId, String emoji) {
        try {
            sse.broadcastChat(jogoId, json.writeValueAsString(Map.of("tipo", "REACAO", "emoji", emoji)));
        } catch (Exception ignored) {}
    }

    private ChatMensagemVM toVM(SalaChatMensagemEntity e) {
        return new ChatMensagemVM(e.getId(), e.getAutorNome(), e.getMensagem(),
                e.getTipo(), e.getCriadoEm().format(FMT));
    }
}
