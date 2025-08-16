package unicam.filiera.dao;

import unicam.filiera.dto.AcquistoItemDto;
import unicam.filiera.dto.AcquistoListaDto;
import unicam.filiera.dto.DatiAcquistoDto;

import java.util.List;

public interface AcquistoDAO {
    /**
     * Inserisce un acquisto nel DB, inclusi gli item associati.
     *
     * @param dati I dati dell'acquisto (acquirente, totale, items, ecc.)
     * @return l'ID generato dell'acquisto (o -1 se errore)
     */
    int salvaAcquisto(DatiAcquistoDto dati);

    List<AcquistoListaDto> findByUsername(String username);

    List<AcquistoItemDto> findItemsByAcquisto(int idAcquisto);

}
