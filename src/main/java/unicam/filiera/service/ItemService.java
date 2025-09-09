package unicam.filiera.service;

import unicam.filiera.dto.*;
import unicam.filiera.model.StatoProdotto;

public interface ItemService {

    /** Crea un nuovo item in base a dto.tipo (PRODOTTO | PACCHETTO | TRASFORMATO). */
    void creaItem(BaseItemDto dto, String creatore);

    /**
     * Modifica un item RIFIUTATO e lo rimette IN_ATTESA .
     * Usa dto.originalName.
     */
    void modificaRifiutato(BaseItemDto dto, String creatore);

    /** Elimina per ID delegando al service specifico. */
    void eliminaById(Long id, ItemTipo tipo, String creatore);

    /** Cambia stato (uso dal Curatore). */
    void cambiaStato(ItemTipo tipo, String nome, String creatore, StatoProdotto nuovoStato, String commento);
}
