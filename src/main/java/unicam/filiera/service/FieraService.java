package unicam.filiera.service;

import unicam.filiera.dto.FieraDto;
import unicam.filiera.entity.FieraEntity;
import unicam.filiera.model.Fiera;

import java.util.List;
import java.util.Optional;

public interface FieraService {

    void creaFiera(FieraDto dto, String creatore);

    void aggiornaFiera(Long id, FieraDto dto, String creatore);

    List<Fiera> getFiereByCreatore(String creatore);

    Optional<FieraEntity> findEntityById(Long id);

    void eliminaById(Long id, String username);
}
