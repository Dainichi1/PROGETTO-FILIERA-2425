package unicam.filiera.service;

import unicam.filiera.model.CategoriaContenuto;
import unicam.filiera.model.CriteriRicerca;
import unicam.filiera.dto.ElementoPiattaformaDto;

import java.util.List;

public interface GestoreContenutiService {

    /**
     * Restituisce tutte le categorie gestibili.
     */
    List<CategoriaContenuto> getCategorieContenuti();

    /**
     * Restituisce i contenuti di una categoria (utenti, prodotti, fiere, ecc.).
     */
    List<ElementoPiattaformaDto> getContenutiCategoria(CategoriaContenuto cat);

    /**
     * Applica filtri e ordinamento in memoria sulla lista dei contenuti.
     */
    List<ElementoPiattaformaDto> filtraOrdinaLista(List<ElementoPiattaformaDto> src, CriteriRicerca criteri);

    /**
     * Stati possibili per la categoria (serve per i menu a tendina dei filtri).
     */
    String[] getPossibiliStati(CategoriaContenuto cat);
}
