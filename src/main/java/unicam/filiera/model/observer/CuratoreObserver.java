package unicam.filiera.model.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import unicam.filiera.model.Prodotto;
import unicam.filiera.model.Pacchetto;

/**
 * Observer unico del Curatore:
 * riceve notifiche sia sui Prodotti che sui Pacchetti.
 */
@Component
public class CuratoreObserver implements OsservatoreProdotto, OsservatorePacchetto {

    private static final Logger log = LoggerFactory.getLogger(CuratoreObserver.class);

    /* =======================
       Notifiche sui Prodotti
    ======================= */
    @Override
    public void notifica(Prodotto prodotto, String evento) {
        switch (evento) {
            case "NUOVO_PRODOTTO" ->
                    log.info("📦 Nuovo prodotto in attesa: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Prodotto approvato: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Prodotto rifiutato: {} (Creato da: {}, Commento: {})",
                            prodotto.getNome(), prodotto.getCreatoDa(), prodotto.getCommento());
            case "ELIMINATO_PRODOTTO" ->
                    log.info("🗑️ Prodotto eliminato: {} (Creato da: {})",
                            prodotto.getNome(), prodotto.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per prodotto: {}", evento, prodotto.getNome());
        }
    }

    /* =======================
       Notifiche sui Pacchetti
    ======================= */
    @Override
    public void notifica(Pacchetto pacchetto, String evento) {
        switch (evento) {
            case "NUOVO_PACCHETTO" ->
                    log.info("📦 Nuovo pacchetto in attesa: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            case "APPROVATO" ->
                    log.info("✅ Pacchetto approvato: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            case "RIFIUTATO" ->
                    log.info("❌ Pacchetto rifiutato: {} (Creato da: {}, Commento: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa(), pacchetto.getCommento());
            case "ELIMINATO_PACCHETTO" ->
                    log.info("🗑️ Pacchetto eliminato: {} (Creato da: {})",
                            pacchetto.getNome(), pacchetto.getCreatoDa());
            default ->
                    log.warn("⚠️ Evento sconosciuto '{}' per pacchetto: {}", evento, pacchetto.getNome());
        }
    }
}
