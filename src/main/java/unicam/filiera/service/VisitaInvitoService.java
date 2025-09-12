package unicam.filiera.service;

import unicam.filiera.dto.VisitaInvitoDto;
import unicam.filiera.entity.VisitaInvitoEntity;
import unicam.filiera.model.VisitaInvito;

import java.util.List;
import java.util.Optional;

public interface VisitaInvitoService {

    void creaVisita(VisitaInvitoDto dto, String creatore);

    void aggiornaVisita(Long id, VisitaInvitoDto dto, String creatore);

    List<VisitaInvito> getVisiteByCreatore(String creatore);

    Optional<VisitaInvitoEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);
}
