package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

/**
 * Service per la gestione della logica di creazione,
 * modifica e recupero Pacchetti.
 */
public interface PacchettoService {

    /**
     * Crea un nuovo pacchetto: validazione → mapping → persistenza → notifica.
     *
     * @throws IllegalArgumentException in caso di validazione fallita.
     */
    void creaPacchetto(PacchettoDto dto, String creatore);

    /**
     * Aggiorna tutti i campi di un pacchetto precedentemente creato
     * (compreso il nome e lo stato torna IN_ATTESA), identificato dal suo nome originale.
     *
     * @param nomeOriginale il nome con cui il pacchetto era stato salvato in precedenza
     * @param dto           i nuovi dati da salvare (nome, descrizione, elementi, certificati, foto…)
     * @param creatore      lo username di chi esegue la modifica
     */
    void aggiornaPacchetto(String nomeOriginale, PacchettoDto dto, String creatore);

    /**
     * Recupera i pacchetti creati da un dato utente.
     */
    List<Pacchetto> getPacchettiCreatiDa(String creatore);

    /**
     * Recupera i pacchetti filtrati per stato.
     */
    List<Pacchetto> getPacchettiByStato(StatoProdotto stato);

    /**
     * Elimina un pacchetto solo se il suo stato è IN_ATTESA o RIFIUTATO.
     *
     * @throws IllegalStateException    se lo stato non consente l’eliminazione.
     * @throws IllegalArgumentException se non trovato.
     */
    void eliminaPacchetto(String nome, String creatore);
}