package unicam.filiera.service;

import unicam.filiera.dto.MarkerDto;

import java.util.List;

public interface MarkerService {

    /**
     * Salva un singolo marker
     */
    MarkerDto saveMarker(MarkerDto dto);

    /**
     * Salva una lista di marker
     */
    List<MarkerDto> saveMarkers(List<MarkerDto> dtos);

    /**
     * Restituisce tutti i marker persistiti
     */
    List<MarkerDto> getAllMarkers();

    /**
     * Elimina un marker specifico
     */
    void deleteMarker(Long id);

    /**
     * Elimina tutti i marker
     */
    void clearMarkers();
}
