package br.com.lectek.copainsider.application.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class SalaJogoSseRegistry {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> chatEmitters  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> notasEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeChatEmitter(Long jogoId) {
        return addEmitter(chatEmitters, jogoId);
    }

    public SseEmitter subscribeNotasEmitter(Long jogoId) {
        return addEmitter(notasEmitters, jogoId);
    }

    public void broadcastChat(Long jogoId, String json) {
        broadcast(chatEmitters, jogoId, json);
    }

    public void broadcastNotas(Long jogoId, String json) {
        broadcast(notasEmitters, jogoId, json);
    }

    private SseEmitter addEmitter(ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> map, Long key) {
        SseEmitter emitter = new SseEmitter(600_000L);
        map.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(emitter);
        Runnable remove = () -> {
            List<SseEmitter> list = map.get(key);
            if (list != null) list.remove(emitter);
        };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(e -> remove.run());
        return emitter;
    }

    private void broadcast(ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> map, Long key, String data) {
        List<SseEmitter> emitters = map.get(key);
        if (emitters == null || emitters.isEmpty()) return;
        List<SseEmitter> dead = new java.util.ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(data));
            } catch (IOException | IllegalStateException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }
}
