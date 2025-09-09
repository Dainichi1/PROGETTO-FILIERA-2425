// ProdottoFactory.java
package unicam.filiera.factory;

import unicam.filiera.dto.ProdottoDto;
import unicam.filiera.model.Prodotto;

public final class ProdottoFactory {
    private ProdottoFactory() { }

    public static Prodotto creaProdotto(ProdottoDto dto, String creatore) {
        return (Prodotto) ItemFactory.creaItem(dto, creatore);
    }
}
