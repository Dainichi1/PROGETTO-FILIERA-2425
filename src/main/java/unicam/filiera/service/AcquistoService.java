package unicam.filiera.service;

import unicam.filiera.dto.AcquistoItemDto;
import unicam.filiera.dto.AcquistoListaDto;
import unicam.filiera.dto.DatiAcquistoDto;

import java.util.List;

public interface AcquistoService {

    void salvaAcquisto(DatiAcquistoDto dto);

    List<AcquistoListaDto> getAcquistiByUsername(String username);

    List<AcquistoItemDto> getItemsByAcquisto(Long id);
}
