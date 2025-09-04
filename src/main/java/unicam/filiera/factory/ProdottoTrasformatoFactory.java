package unicam.filiera.factory;

import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.ProdottoTrasformato;

/**
 * Facciata semplificata per la creazione di {@link ProdottoTrasformato}.
 * Internamente delega a {@link ItemFactory}, così il codice esistente
 * non deve essere modificato.
 */
public final class ProdottoTrasformatoFactory {

    private ProdottoTrasformatoFactory() {
    }

    public static ProdottoTrasformato creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
        return (ProdottoTrasformato) ItemFactory.creaItem(ItemFactory.TipoItem.PRODOTTO_TRASFORMATO, dto, creatore);
    }
}
