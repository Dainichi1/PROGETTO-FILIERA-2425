package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;
import unicam.filiera.model.StatoProdotto;

import java.util.List;

public interface PacchettoService {
    /**
     * Crea un nuovo pacchetto: validazione → mapping → persistenza → notifica.
     * @throws IllegalArgumentException in caso di validazione fallita.
     */
    void creaPacchetto(PacchettoDto dto, String creatore);

    /** Recupera i pacchetti creati da un dato utente. */
    List<Pacchetto> getPacchettiCreatiDa(String creatore);

    /** Recupera i pacchetti filtrati per stato. */
    List<Pacchetto> getPacchettiByStato(StatoProdotto stato);

    /**
     * Elimina un pacchetto solo se il suo stato è IN_ATTESA o RIFIUTATO.
     * @throws IllegalStateException se lo stato non consente l’eliminazione.
     * @throws IllegalArgumentException se non trovato.
     */
    void eliminaPacchetto(String nome, String creatore);
}
