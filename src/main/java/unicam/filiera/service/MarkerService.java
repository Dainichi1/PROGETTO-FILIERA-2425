package unicam.filiera.service;

import unicam.filiera.entity.MarkerEntity;

import java.util.List;

public interface MarkerService {

    /**
     * Salva un singolo marker
     */
    MarkerEntity saveMarker(MarkerEntity marker);

    /**
     * Salva una lista di marker
     */
    List<MarkerEntity> saveMarkers(List<MarkerEntity> markers);

    /**
     * Restituisce tutti i marker persistiti
     */
    List<MarkerEntity> getAllMarkers();

    /**
     * Elimina un marker specifico
     */
    void deleteMarker(Long id);

    /**
     * Elimina tutti i marker
     */
    void clearMarkers();
}
