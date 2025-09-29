package unicam.filiera.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationServiceImpl implements NotificationService {

    // mappa username → emitter attivo
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @Override
    public SseEmitter addEmitter(String username) {
        SseEmitter emitter = new SseEmitter(0L); // senza timeout
        emitters.put(username, emitter);

        // cleanup automatico
        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError(e -> emitters.remove(username));

        return emitter;
    }

    @Override
    public void notifyUser(String username, String event, String message) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(event)     // tipo evento
                        .data(message)); // payload
            } catch (IOException e) {
                emitters.remove(username);
            }
        }
    }
}
