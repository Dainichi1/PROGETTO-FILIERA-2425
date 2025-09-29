package unicam.filiera.service;

import unicam.filiera.dto.PacchettoDto;
import unicam.filiera.entity.PacchettoEntity;
import unicam.filiera.model.StatoProdotto;

import java.util.List;
import java.util.Optional;

public interface PacchettoService {

    void creaPacchetto(PacchettoDto dto, String creatore);

    void aggiornaPacchetto(Long id, PacchettoDto dto, String creatore);

    List<PacchettoDto> getPacchettiCreatiDa(String creatore);

    List<PacchettoDto> getPacchettiByStato(StatoProdotto stato);

    void cambiaStatoPacchetto(String nome, String creatore, StatoProdotto nuovoStato, String commento);

    Optional<PacchettoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);

    Optional<PacchettoDto> findDtoById(Long id);

}

