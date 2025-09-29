package unicam.filiera.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.RichiestaEliminazioneProfilo;

/**
 * Osservatore che gestisce le notifiche per l’utente
 * (ad esempio log, email, websocket o popup UI).
 */
@Component
public class UtenteEliminazioneProfiloObserver implements OsservatoreEliminazioneProfilo {

    private static final Logger log = LoggerFactory.getLogger(UtenteEliminazioneProfiloObserver.class);

    @Override
    public void notifica(RichiestaEliminazioneProfilo richiesta, String evento, String messaggio) {
        switch (evento) {
            case "APPROVATA" -> log.info("✅ Profilo eliminato per utente {}. Messaggio: {}", richiesta.getUsername(), messaggio);
            case "RIFIUTATA" -> log.info("❌ Richiesta eliminazione rifiutata per utente {}. Messaggio: {}", richiesta.getUsername(), messaggio);
            default -> log.warn("⚠ Evento sconosciuto '{}' per richiesta {}", evento, richiesta.getId());
        }
    }
}
