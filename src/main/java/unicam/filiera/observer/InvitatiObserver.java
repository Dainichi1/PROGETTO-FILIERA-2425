package unicam.filiera.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.VisitaInvito;

@Component
public class InvitatiObserver implements OsservatoreVisitaInvito {

    private static final Logger log = LoggerFactory.getLogger(InvitatiObserver.class);

    @Override
    public void notifica(VisitaInvito visita, String evento) {
        switch (evento) {
            case "NUOVA_VISITA_PUBBLICATA" -> {
                for (String destinatario : visita.getDestinatari()) {
                    log.info("📩 Nuova visita disponibile per {}: {} (dal {} al {})",
                            destinatario,
                            visita.getNome(),
                            visita.getDataInizio(),
                            visita.getDataFine());
                }
            }
            case "VISITA_ELIMINATA" -> {
                for (String destinatario : visita.getDestinatari()) {
                    log.info("❌ Visita eliminata: {} (invitato: {})", visita.getNome(), destinatario);
                }
            }
            case "VISITA_AGGIORNATA" -> {
                for (String destinatario : visita.getDestinatari()) {
                    log.info("🔄 Visita aggiornata: {} (invitato: {})", visita.getNome(), destinatario);
                }
            }
            default -> log.warn("⚠️ Evento sconosciuto '{}' per visita: {}", evento, visita.getNome());
        }
    }
}
