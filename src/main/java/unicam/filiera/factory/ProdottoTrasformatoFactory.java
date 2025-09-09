package unicam.filiera.factory;

import unicam.filiera.dto.ProdottoTrasformatoDto;
import unicam.filiera.model.ProdottoTrasformato;

public final class ProdottoTrasformatoFactory {
    private ProdottoTrasformatoFactory() { }

    public static ProdottoTrasformato creaProdottoTrasformato(ProdottoTrasformatoDto dto, String creatore) {
        return (ProdottoTrasformato) ItemFactory.creaItem(dto, creatore);
    }
}
