package unicam.filiera.service;

import unicam.filiera.dto.ProdottoTrasformatoPathDto;

public interface ProdottoTrasformatoPathService {

    /**
     * Restituisce le coordinate del prodotto trasformato
     * e di tutte le fasi (prodotti origine).
     */
    ProdottoTrasformatoPathDto getPath(Long trasformatoId);
}
