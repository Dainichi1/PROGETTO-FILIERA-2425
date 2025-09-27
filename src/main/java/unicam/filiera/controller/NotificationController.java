package unicam.filiera.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import unicam.filiera.model.RichiestaEliminazioneProfilo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/sse")
public class NotificationController {

    // mappa username → emitter attivo
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    @GetMapping(path = "/subscribe/{username}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable String username) {
        SseEmitter emitter = new SseEmitter(0L); // senza timeout
        emitters.put(username, emitter);

        emitter.onCompletion(() -> emitters.remove(username));
        emitter.onTimeout(() -> emitters.remove(username));
        emitter.onError(e -> emitters.remove(username));

        return emitter;
    }

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
