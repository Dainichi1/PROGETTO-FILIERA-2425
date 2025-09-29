package unicam.filiera.observer;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import unicam.filiera.model.RichiestaEliminazioneProfilo;

import java.util.List;

/**
 * Gestisce la notifica degli eventi agli osservatori delle richieste di eliminazione profilo.
 */
@Component
public class EliminazioneProfiloNotifier {

    private final List<OsservatoreEliminazioneProfilo> osservatori;

    public EliminazioneProfiloNotifier(List<OsservatoreEliminazioneProfilo> osservatori) {
        this.osservatori = osservatori;
    }

    @PostConstruct
    public void init() {
        System.out.println("✅ EliminazioneProfiloNotifier inizializzato con " + osservatori.size() + " osservatori registrati.");
    }

    public void notificaApprovata(RichiestaEliminazioneProfilo richiesta) {
        notificaTutti(richiesta, "APPROVATA",
                "Il tuo profilo è stato eliminato (richiesta ID " + richiesta.getId() + "). Verrai riportato alla schermata iniziale...");
    }

    public void notificaRifiutata(RichiestaEliminazioneProfilo richiesta) {
        notificaTutti(richiesta, "RIFIUTATA",
                "La tua richiesta di eliminazione profilo è stata rifiutata dal Gestore.");
    }

    private void notificaTutti(RichiestaEliminazioneProfilo richiesta, String evento, String messaggio) {
        for (OsservatoreEliminazioneProfilo o : osservatori) {
            o.notifica(richiesta, evento, messaggio);
        }
    }
}
