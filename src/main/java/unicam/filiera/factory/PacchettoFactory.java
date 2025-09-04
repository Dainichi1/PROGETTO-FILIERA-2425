package unicam.filiera.factory;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.model.Pacchetto;

/**
 * Facciata semplificata per la creazione di {@link Pacchetto}.
 * Internamente delega a {@link ItemFactory}, così il codice esistente
 * non deve essere modificato.
 */
public final class PacchettoFactory {

    private PacchettoFactory() {
    }

    public static Pacchetto creaPacchetto(PacchettoDto dto, String creatore) {
        return (Pacchetto) ItemFactory.creaItem(ItemFactory.TipoItem.PACCHETTO, dto, creatore);
    }
}
