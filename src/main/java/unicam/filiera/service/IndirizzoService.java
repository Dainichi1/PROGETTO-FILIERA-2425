package unicam.filiera.service;

import unicam.filiera.dto.IndirizzoDto;
import unicam.filiera.dto.GeocodedIndirizzoDto;

import java.util.List;

public interface IndirizzoService {

    /**
     * Recupera tutti gli indirizzi disponibili (solo contenuti pubblicati/approvati).
     * Include: Prodotti, Pacchetti, Fiere, Visite, Prodotti Trasformati.
     */
    List<IndirizzoDto> getIndirizziDisponibili();

    /**
     * Converte un indirizzo testuale in lat/lng (geocoding).
     */
    GeocodedIndirizzoDto geocodeIndirizzo(IndirizzoDto indirizzo);
}
