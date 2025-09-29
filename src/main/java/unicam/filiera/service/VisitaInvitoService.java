package unicam.filiera.service;

import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.entity.VisitaInvitoEntity;
import unicam.filiera.model.StatoEvento;

import java.util.List;
import java.util.Optional;

public interface VisitaInvitoService {

    void creaVisita(VisitaInvitoDto dto, String creatore);

    void aggiornaVisita(Long id, VisitaInvitoDto dto, String creatore);

    List<VisitaInvitoDto> getVisiteByCreatore(String creatore);

    List<VisitaInvitoDto> getVisiteByStato(StatoEvento stato);

    List<VisitaInvitoDto> getVisiteByRuoloDestinatario(String ruolo);

    Optional<VisitaInvitoEntity> findEntityById(Long id);

    Optional<VisitaInvitoDto> findDtoById(Long id);

    void eliminaById(Long id, String username);
}
