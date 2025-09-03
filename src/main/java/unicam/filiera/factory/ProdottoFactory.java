package unicam.filiera.factory;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;

/**
 * Facciata semplificata per la creazione di {@link Prodotto}.
 * Internamente delega a {@link ItemFactory}, così il codice esistente
 * non deve essere modificato.
 */
public final class ProdottoFactory {

    private ProdottoFactory() {
    }

    public static Prodotto creaProdotto(ProdottoDto dto, String creatore) {
        return ItemFactory.creaProdotto(dto, creatore);
    }
}
