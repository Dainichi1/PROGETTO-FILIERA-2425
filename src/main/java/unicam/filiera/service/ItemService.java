package unicam.filiera.service;

import unicam.filiera.dto.*;
import unicam.filiera.model.StatoProdotto;

public interface ItemService {

    void creaItem(BaseItemDto dto, String creatore);

    void modificaRifiutato(BaseItemDto dto, String creatore);

    void eliminaNonApprovato(ItemTipo tipo, Long id, String username);

    void cambiaStato(ItemTipo tipo, String nome, String creatore, StatoProdotto nuovoStato, String commento);
}
